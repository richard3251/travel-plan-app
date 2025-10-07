package com.travelapp.backend.domain.file.service;

import com.travelapp.backend.domain.file.entity.FileInfo;
import com.travelapp.backend.domain.file.repository.FileInfoRepository;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Slf4j
@RequiredArgsConstructor
public class ThumbnailService {

    private final S3Client s3Client;
    private final FileInfoRepository fileInfoRepository;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${file.upload.thumbnail.width}")
    private int thumbnailWidth;

    @Value("${file.upload.thumbnail.height}")
    private int thumbnailHeight;

    @Value("${file.upload.thumbnail.quality}")
    private double thumbnailQuality;

    /**
     * 비동기 썸네일 생성
     */
    @Async
    public void createThumbnailAsync(FileInfo fileInfo) {
        log.info("썸네일 생성 시작 - 파일 ID: {}, S3 키: {}", fileInfo.getId(), fileInfo.getS3Key());

        try {
            // S3에서 원본 이미지 다운로드
            byte[] originalImageData = downloadImageFromS3(fileInfo.getS3Key());

            // 썸네일 생성
            byte[] thumbnailData = createThumbnail(originalImageData);

            // 썸네일 S3 키 생성
            String thumbnailS3Key = generateThumbnailS3Key(fileInfo.getS3Key());

            // 썸네일을 S3에 업로드
            uploadThumbnailToS3(thumbnailS3Key, thumbnailData, "image/jpeg");

            // 썸네일 URL 생성
            String thumbnailUrl = generateS3Url(thumbnailS3Key);

            // FileInfo에 썸네일 정보 업데이트
            fileInfo.updateThumbnailInfo(thumbnailS3Key, thumbnailUrl);
            fileInfoRepository.save(fileInfo);


        } catch (Exception e) {
            log.error("썸네일 생성 실패 - 파일 ID: {}, S3 키: {}", fileInfo.getId(), fileInfo.getS3Key(), e);
            // 썸네일 생성 실패는 전체 업로드를 실패시키지 않음
        }
    }

    /**
     * S3(AWS)에서 이미지 다운로드
     */
    private byte[] downloadImageFromS3(String s3Key) throws IOException {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

            return s3Client.getObject(getObjectRequest).readAllBytes();
        } catch (Exception e) {
            log.error("S3에서 이미지 다운로드 실패 - S3 키: {}", s3Key, e);
            throw new IOException("S3 이미지 다운로드 실패", e);
        }
    }

    /**
     * 지정된 크기 품질로 수정 및 썸네일 생성후 반환
     */
    private byte[] createThumbnail(byte[] originalImageData) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(originalImageData);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // BufferedImage로 읽기
            BufferedImage originalImage = ImageIO.read(inputStream);
            if (originalImage == null) {
                throw new IOException("이미지 파일을 읽을 수 없습니다");
            }

            // 썸네일 생성 (비율 유지하면서 크기 조정)
            Thumbnails.of(originalImage)
                .size(thumbnailWidth, thumbnailHeight)
                .outputQuality(thumbnailQuality)
                .outputFormat("jpg")
                .toOutputStream(outputStream);

            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("썸네일 생성 실패", e);
            throw new IOException("썸네일 생성 실패", e);
        }
    }

    /**
     *  썸네일 S3 키 생성
     */
    private String generateThumbnailS3Key(String originalS3Key) {
        // uploads/2024/12/01/uuid_filename.jpg -> thumbnails/2024/12/01/thumb_uuid_filename.jpg
        String[] parts = originalS3Key.split("/");
        if (parts.length >= 4) {
            String fileName = parts[parts.length - 1];
            String datePath = String.join("/", parts[1], parts[2], parts[3]);
            return String.format("thumbnails/%s/thumb_%s", datePath, fileName);
        } else {
            // 예상과 다른 경로 구조인 경우 기본 방식 사용
            String uuid = UUID.randomUUID().toString().substring(0, 8);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            return String.format("thumbnails/%s/thumb_%s.jpg", timestamp, uuid);
        }
    }

    /**
     * 썸네일을 S3에 업로드
     */
    private void uploadThumbnailToS3(String thumbnailS3Key, byte[] thumbnailData, String contentType) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(thumbnailS3Key)
                .contentType(contentType)
                .contentLength((long) thumbnailData.length)
                .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(thumbnailData));

            log.debug("썸네일 S3 업로드 완료 - 키: {}, 크기: {}bytes", thumbnailS3Key, thumbnailData.length);
        } catch (Exception e) {
            log.error("썸네일 S3 업로드 실패 - 키: {}", thumbnailS3Key, e);
            throw new RuntimeException("썸네일 S3 업로드 실패");
        }
    }

    /**
     * S3 URL 생성
     */
    private String generateS3Url(String s3Key) {
        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, s3Key);
    }






























}

package com.travelapp.backend.domain.file.service;

import com.travelapp.backend.domain.file.dto.request.PresignedUrlRequest;
import com.travelapp.backend.domain.file.dto.response.PresignedUrlResponse;
import com.travelapp.backend.domain.file.entity.FileInfo;
import com.travelapp.backend.domain.file.entity.FileType;
import com.travelapp.backend.global.exception.BusinessException;
import com.travelapp.backend.global.exception.dto.ErrorCode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3PresignedUrlService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.presigned-url-expiration}")
    private int presignedUrlExpiration;

    @Value("${file.upload.max-file-size}")
    private long maxFileSize;

    @Value("${file.upload.allowed-types}")
    private String allowedTypes;

    /**
     * Pre-signed URL 생성
     */
    public PresignedUrlResponse generatePresignedUrl(PresignedUrlRequest request, Long uploadedBy) {
        log.info("Pre-signed URL 생성 요청 - 파일: {}", request.getFileName(), uploadedBy);

        // 파일 유효성 검증
        validateUploadRequest(request);

        // S3 키 생성
        String s3key = generateS3Key(request.getFileName());

        // FileInfo 메타데이터 생성 (PENDING 상태)
        FileInfo fileInfo = createPendingFileInfo(request, s3key, uploadedBy);

        // Pre-signed URL 생성
        String presignedUrl = createPresignedUrl(s3key, request.getContentType());

        // 만료 시간 계산
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(presignedUrlExpiration);

        log.info("Pre-signed URL 생성 완료 - 파일 ID: {}, S3 키 : {}", fileInfo.getId(), s3key);

        return PresignedUrlResponse.builder()
            .fileId(fileInfo.getId())
            .presignedUrl(presignedUrl)
            .s3Key(s3key)
            .headers(PresignedUrlResponse.UploadHeaders.builder()
                .contentType(request.getContentType())
                .contentLength(request.getFileSize())
                .build())
            .expiresAt(expiresAt)
            .guide(PresignedUrlResponse.UploadGuide.builder()
                .method("PUT")
                .completeApi("POST /api/files/upload-complete")
                .timeoutMinutes(presignedUrlExpiration / 60)
                .build())
            .build();
    }

    /**
     * 업로드 요청 유효성 검증
     */
    private void validateUploadRequest(PresignedUrlRequest request) {
        // 파일 크기 검증
        if (request.getFileSize() > maxFileSize) {
            log.warn("파일 크기 초과 - 요청: {}bytes, 최대: {}bytes", request.getFileSize(), maxFileSize);
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 콘텐츠 타입 검증
        if (!isAllowedContentType(request.getContentType())) {
            log.warn("허용되지 않은 파일 타입 - {}", request.getContentType());
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 파일명 검증
        if (request.getFileName() == null || request.getFileName().trim().isEmpty()) {
            log.warn("유효하지 않은 파일명");
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 파일명에서 위험한 문자 확인
        if (containsDangerousCharacters(request.getFileName())) {
            log.warn("위험한 문자가 포함된 파일명 - {}", request.getFileName());
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        log.debug("파일 업로드 요청 유효성 검증 통과 - {}", request.getFileName());
    }

    /**
     * 허용된 콘텐츠 타입인지 확인
     */
    private boolean isAllowedContentType(String contentType) {
        List<String> allowedTypeList = Arrays.asList(allowedTypes.split(","));
        return allowedTypeList.contains(contentType.trim());
    }

    /**
     * 파일명에 위험한 문자가 포함되어 있는지 확인
     */
    private boolean containsDangerousCharacters(String fileName) {
        String[] dangerousChars = {"..", "/", "\\", ":", "*", "?", "\"", "<", ">", "|"};
        for (String dangerousChar : dangerousChars) {
            if (fileName.contains(dangerousChar)) {
                return true;
            }
        }
        return false;
    }

    /**
     * S3 키 생성 (경로 포함)
     */
    private String generateS3Key(String originalFileName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uuid = UUID.randomUUID().toString().substring(0,8);
        String extension = getFileExtension(originalFileName);
        String sanitizedFileName = sanitizeFileName(originalFileName);

        return String.format("uploads/%s/%s_%s%s", timestamp, uuid, sanitizedFileName, extension);
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    /**
     * 파일명 정리 (안전한 문자만 유지)
     */
    private String sanitizeFileName(String fileName) {
        // 확장자 제거
        String nameWithoutExtension = fileName;
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0) {
            nameWithoutExtension = fileName.substring(0, lastDotIndex);
        }

        // 안전한 문자만 유지 (알파벳, 숫자, 하이픈, 언더스코어, 한글)
        return nameWithoutExtension.replaceAll("[^a-zA-Z0-9\\\\-_가-힣]", "_")
            .substring(0, Math.min(nameWithoutExtension.length(), 50)); // 길이 제한
    }

    /**
     * PENDING 상태의 FileInfo 생성
     */
    private FileInfo createPendingFileInfo(PresignedUrlRequest request, String s3Key, Long uploadedBy) {
        String s3Url = generateS3Url(s3Key);
        FileType fileType = FileType.fromContentType(request.getContentType());

        return FileInfo.builder()
            .originalName(request.getFileName())
            .s3Key(s3Key)
            .s3Url(s3Url)
            .fileSize(request.getFileSize())
            .contentType(request.getContentType())
            .fileType(fileType)
            .uploadedBy(uploadedBy)
            .build();
    }

    /**
     * S3 URL 생성
     */
    private String generateS3Url(String s3Key) {
        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, s3Key);
    }

    /**
     * Pre-signed PUT URL 생성
     */
    private String createPresignedUrl(String s3Key, String contentType) {
        try (S3Presigner presigner = S3Presigner.create()) {

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(contentType)
                .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(presignedUrlExpiration))
                .putObjectRequest(putObjectRequest)
                .build();

            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);

            return presignedRequest.url().toString();
        } catch (Exception e) {
            log.error("Pre-signed URL 생성 실패 - S3 키: {}", s3Key, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }



}

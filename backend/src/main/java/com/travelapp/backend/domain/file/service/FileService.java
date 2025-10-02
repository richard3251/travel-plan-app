package com.travelapp.backend.domain.file.service;

import com.travelapp.backend.domain.file.dto.request.PresignedUrlRequest;
import com.travelapp.backend.domain.file.dto.response.PresignedUrlResponse;
import com.travelapp.backend.domain.file.entity.FileInfo;
import com.travelapp.backend.domain.file.entity.FileType;
import com.travelapp.backend.domain.file.repository.FileInfoRepository;
import com.travelapp.backend.domain.file.repository.TripImageRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class FileService {

    private final FileInfoRepository fileInfoRepository;
    private final TripImageRepository tripImageRepository;
    private final S3PresignedUrlService s3PresignedUrlService;

    /**
     * 일반 파일 업로드용 Pre-signed URL 발급
     */
    public PresignedUrlResponse generatePresignedUrl(@Valid PresignedUrlRequest request, Long uploadedBy) {
        log.info("일반 파일 Pre-signed URL 발급 요청 - 사용자: {}, 파일: {}", uploadedBy, request.getFileName());

        // Pre-signed URL 생성 및 메타데이터 저장
        PresignedUrlResponse response = s3PresignedUrlService.generatePresignedUrl(request, uploadedBy);

        // DB에 PENDING 상태로 저장
        FileInfo fileInfo = FileInfo.builder()
            .id(response.getFileId())
            .originalName(request.getFileName())
            .s3Key(response.getS3Key())
            .s3Url(generateS3Url(response.getS3Key()))
            .fileSize(request.getFileSize())
            .contentType(request.getContentType())
            .fileType(FileType.fromContentType(request.getContentType()))
            .uploadedBy(uploadedBy)
            .build();

        fileInfoRepository.save(fileInfo);

        log.info("일반 파일 Pre-signed URL 발급 완료 - 파일 ID: {}", fileInfo.getId());
        return response;
    }

    /**
     * S3 URL 생성
     */
    private String generateS3Url(String s3Key) {
        return String.format("https://%s.s3.amazonaws.com/%s",
            "travel-app-files", s3Key); // 실제 버킷명으로 교체 필요
    }






















}

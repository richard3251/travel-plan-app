package com.travelapp.backend.domain.file.service;

import com.travelapp.backend.domain.file.dto.request.PresignedUrlRequest;
import com.travelapp.backend.domain.file.dto.request.UploadCompleteRequest;
import com.travelapp.backend.domain.file.dto.response.FileUploadResponse;
import com.travelapp.backend.domain.file.dto.response.PresignedUrlResponse;
import com.travelapp.backend.domain.file.dto.response.TripImageResponse;
import com.travelapp.backend.domain.file.entity.FileInfo;
import com.travelapp.backend.domain.file.entity.TripImage;
import com.travelapp.backend.domain.file.entity.UploadStatus;
import com.travelapp.backend.domain.file.repository.FileInfoRepository;
import com.travelapp.backend.domain.file.repository.TripImageRepository;
import com.travelapp.backend.domain.trip.entity.Trip;
import com.travelapp.backend.domain.trip.service.TripService;
import com.travelapp.backend.global.exception.BusinessException;
import com.travelapp.backend.global.exception.dto.ErrorCode;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final ThumbnailService thumbnailService;
    private final TripService tripService;

    /**
     * 일반 파일 업로드용 Pre-signed URL 발급
     */
    public PresignedUrlResponse generatePresignedUrl(@Valid PresignedUrlRequest request, Long uploadedBy) {
        log.info("일반 파일 Pre-signed URL 발급 요청 - 사용자: {}, 파일: {}", uploadedBy, request.getFileName());

        // Pre-signed URL 생성 (S3PresignedUrlService에서 FileInfo 저장까지 처리)
        PresignedUrlResponse response = s3PresignedUrlService.generatePresignedUrl(request, uploadedBy);

        FileInfo savedFileInfo = fileInfoRepository.findById(response.getFileId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        log.info("일반 파일 Pre-signed URL 발급 완료 - 파일 ID: {}", savedFileInfo.getId());
        return response;
    }

    /**
     * 여행 이미지 업로드용 Pre-signed URL 발급
     */
    public PresignedUrlResponse generateTripImagePresignedUrl(PresignedUrlRequest request, Long uploadedBy) {
        log.info("여행 이미지 Pre-signed URL 발급 요청 - 여행: {}, 사용자: {}", request.getTripId(), uploadedBy);

        // 여행 존재 및 권한 확인
        Trip trip = tripService.findTripWithOwnerValidation(request.getTripId());

        // Pre-signed URL 생성 (S3PresignedUrlService에서 FileInfo 저장까지 처리)
        PresignedUrlResponse response = s3PresignedUrlService.generatePresignedUrl(request, uploadedBy);

        // 저장된 FileInfo 조회
        FileInfo savedFileInfo = fileInfoRepository.findById(response.getFileId())
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        // 여행 이미지 메타데이터도 미리 생성 (PENDING 상태)
        createPendingTripImage(trip, savedFileInfo, request);

        log.info("여행 이미지 Pre-signed URL 발급 완료 - 파일 ID: {}", savedFileInfo.getId());
        return response;
    }

    /**
     * 업로드 완료 처리
     */
    public FileUploadResponse completeUpload(UploadCompleteRequest request, Long uploadedBy) {
        log.info("업로드 완료 처리 - 파일 ID: {}, 성공: {}", request.getFileId(), request.getSuccess());

        FileInfo fileInfo = fileInfoRepository.findById(request.getFileId())
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        // 권한 확인
        if (!fileInfo.getUploadedBy().equals(uploadedBy)) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
        }

        if (Boolean.TRUE.equals(request.getSuccess())) {
            // 업로드 성공 처리
            handleSuccessfulUpload(fileInfo);
        } else {
            // 업로드 실패 처리
            handleFailedUpload(fileInfo, request.getErrorMessage());
        }

        FileInfo updatedFileInfo = fileInfoRepository.save(fileInfo);
        log.info("업로드 완료 처리 완료 - 파일 ID: {}, 상태: {}", updatedFileInfo.getId(),
            updatedFileInfo.getUploadStatus());

        return FileUploadResponse.from(updatedFileInfo);
    }

    /**
     * 사용자 파일 목록 조회 (완료된 파일만)
     */
    @Transactional(readOnly = true)
    public Page<FileUploadResponse> getUserFiles(Long userId, Pageable pageable) {
        Page<FileInfo> files = fileInfoRepository
            .findByUploadedByAndUploadStatusAndIsDeletedFalseOrderByCreatedAtDesc(
                userId, UploadStatus.COMPLETED, pageable);

        return files.map(FileUploadResponse::from);
    }

    /**
     * 여행 이미지 목록 조회 (완료된 이미지만)
     */
    @Transactional(readOnly = true)
    public List<TripImageResponse> getTripImages(Long tripId) {
        List<TripImage> tripImages = tripImageRepository
            .findCompletedImagesByTripIdOrderByDisplayOrder(tripId);

        return tripImages.stream()
            .map(TripImageResponse::from)
            .toList();
    }

    /**
     * 여행 커버 이미지 조회
     */
    @Transactional(readOnly = true)
    public TripImageResponse getTripCoverImage(Long tripId) {
        TripImage coverImage = tripImageRepository.findCoverImageByTripId(tripId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        return TripImageResponse.from(coverImage);
    }

    /**
     * 파일 삭제 (논리 삭제)
     */
    public void deleteFile(Long fileId, Long userId) {
        FileInfo fileInfo = fileInfoRepository
            .findByIdAndUploadStatusAndIsDeletedFalse(fileId, UploadStatus.COMPLETED)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        // 권한 확인
        if (!fileInfo.getUploadedBy().equals(userId)) {
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        // 논리 삭제
        fileInfo.markAsDeleted();
        fileInfoRepository.save(fileInfo);

        log.info("파일 삭제 완료 - ID: {}, 사용자: {}", fileId, userId);
    }

    /**
     * 여행 이미지 삭제
     */
    public void deleteTripImage(Long imageId, Long userId) {
        TripImage tripImage = tripImageRepository.findCompletedImageByIdAndTripOwnerId(imageId, userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        // 여행 이미지 삭제
        tripImageRepository.delete(tripImage);

        // 파일 논리 삭제
        FileInfo fileInfo = tripImage.getFileInfo();
        fileInfo.markAsDeleted();
        fileInfoRepository.save(fileInfo);

        log.info("여행 이미지 삭제 완료 - ID: {}, 사용자: {}", imageId, userId);
    }

    /**
     * 여행 이미지 순서 변경
     */
    public void updateTripImageOrder(Long imageId, Integer newOrder, Long userId) {
        TripImage tripImage = tripImageRepository.findCompletedImageByIdAndTripOwnerId(imageId, userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        Integer currentOrder = tripImage.getDisplayOrder();
        Long tripId = tripImage.getTrip().getId();

        if (newOrder.equals(currentOrder)) {
            return; // 순서 변경 없음
        }

        // 순서 조정 로직
        if (newOrder > currentOrder) {
            tripImageRepository.incrementDisplayOrder(tripId, currentOrder + 1);
        } else {
            tripImageRepository.incrementDisplayOrder(tripId, newOrder);
        }

        // 현재 이미지의 순서 변경
        tripImage.updateDisplayOrder(newOrder);
        tripImageRepository.save(tripImage);

        log.info("여행 이미지 순서 변경 완료 - ID: {}, {} -> {}", imageId, currentOrder, newOrder);
    }

    /**
     * PENDING 상태 여행 이미지 생성
     */
    private void createPendingTripImage(Trip trip, FileInfo fileInfo, PresignedUrlRequest request) {
        // 커버 이미지 설정 처리
        if (Boolean.TRUE.equals(request.getIsCoverImage())) {
            tripImageRepository.clearCoverImages(trip.getId());
        }

        // 표시 순서 설정
        Integer displayOrder = request.getDisplayOrder();
        if (displayOrder == null) {
            displayOrder = tripImageRepository.getMaxDisplayOrder(trip.getId()) + 1;
        } else {
            tripImageRepository.incrementDisplayOrder(trip.getId(), displayOrder);
        }

        // 여행 이미지 정보 저장
        TripImage tripImage = TripImage.builder()
            .trip(trip)
            .fileInfo(fileInfo)
            .isCoverImage(Boolean.TRUE.equals(request.getIsCoverImage()))
            .displayOrder(displayOrder)
            .caption(request.getCaption())
            .build();

        tripImageRepository.save(tripImage);
    }

    /**
     * 업로드 실패 처리
     */
    private void handleFailedUpload(FileInfo fileInfo, String errorMessage) {
        log.warn("파일 업로드 실패 - 파일 ID: {}, 오류: {}", fileInfo.getId(), errorMessage);

        fileInfo.markAsFailed();

        // S3에서 파일 삭제 (업로드가 부분적으로 진행되었을 수 있음)
        s3PresignedUrlService.deleteFileFromS3(fileInfo.getS3Key());
    }

    /**
     * 업로드 성공 처리
     */
    private void handleSuccessfulUpload(FileInfo fileInfo) {
        // S3에 파일이 실제로 존재하는지 확인
        if (!s3PresignedUrlService.existsInS3(fileInfo.getS3Key())) {
            log.warn("S3에 파일이 존재하지 않음 - {}", fileInfo.getS3Key());
            fileInfo.markAsFailed();
            return;
        }

        // 업로드 완료 상태로 변경
        fileInfo.markAsCompleted();

        // 이미지인 경우 썸네일 생성 (비동기)
        if (fileInfo.isImage()) {
            thumbnailService.createThumbnailAsync(fileInfo);
        }
    }

    /**
     * S3 URL 생성 (현재 S3PreSignedUrlService에서 처리하므로 사용하지 않음)
     */
    @SuppressWarnings("unused")
    private String generateS3Url(String s3Key) {
        return String.format("https://%s.s3.amazonaws.com/%s",
            "travel-app-files", s3Key); // 실제 버킷명으로 교체 필요
    }


}

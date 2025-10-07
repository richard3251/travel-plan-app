package com.travelapp.backend.domain.file.repository;

import com.travelapp.backend.domain.file.entity.FileInfo;
import com.travelapp.backend.domain.file.entity.FileType;
import com.travelapp.backend.domain.file.entity.UploadStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, Long> {

    /**
     * S3 키로 파일 조회
     */
    Optional<FileInfo> findByS3KeyAndIsDeletedFalse(String s3Key);

    /**
     * 완료된 파일만 조회
     */
    Optional<FileInfo> findByIdAndUploadStatusAndIsDeletedFalse(Long id, UploadStatus status);

    /**
     * 특정 사용자가 업로드한 완료된 파일 목록 조회 (페이징)
     */
    Page<FileInfo> findByUploadedByAndUploadStatusAndIsDeletedFalseOrderByCreatedAtDesc(
        Long uploadedBy, UploadStatus status, Pageable pageable
    );

    /**
     * 특정 타입의 완료된 파일들 조회
     */
    List<FileInfo> findByFileTypeAndUploadStatusAndIsDeletedFalseOrderByCreatedAtDesc(
        FileType fileType, UploadStatus status);

    /**
     * 특정 사용자의 특정 타입 완료된 파일들 조회
     */
    List<FileInfo> findByUploadedByAndFileTypeAndUploadStatusAndIsDeletedFalseOrderByCreatedAtDesc(
        Long uploadedBy, FileType fileType, UploadStatus status);

    /**
     * 특정 기간 이전에 생성된 미완료 파일들 조회 (정리용)
     */
    @Query("SELECT f FROM FileInfo f WHERE f.uploadStatus = :status AND f.createdAt < :beforeDate")
    List<FileInfo> findPendingFilesBefore(@Param("status") UploadStatus status,
        @Param("beforeDate")LocalDateTime beforeDate);

    /**
     * 특정 기간 이전에 삭제된 파일들 조회 (정리용)
     */
    @Query("SELECT f FROM FileInfo f WHERE f.isDeleted = true AND f.deletedAt < :beforeDate")
    List<FileInfo> findDeletedFilesBefore(@Param("beforeDate") LocalDateTime beforeDate);

    /**
     * 사용자별 총 파일 크기 계산 (완료된 파일만)
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileInfo f " +
        "WHERE f.uploadedBy = :userId AND f.uploadStatus = 'COMPLETED' AND f.isDeleted = false")
    Long getTotalFileSizeByUser(@Param("userId") Long userId);

    /**
     * 고아 파일 조회 (어떤 엔티티와도 연결되지 않은 완료된 파일)
     */
    @Query("SELECT f FROM FileInfo f WHERE f.uploadStatus = 'COMPLETED' AND f.isDeleted = false " +
        "AND f.id NOT IN (SELECT ti.fileInfo.id FROM TripImage ti) " +
        "AND f.createdAt < :beforeDate")
    List<FileInfo> findOrphanFiles(@Param("beforeDate") LocalDateTime beforeDate);

    /**
     * 업로드 상태별 파일 개수 조회
     */
    long countByUploadStatusAndIsDeletedFalse(UploadStatus status);

    /**
     * 특정 사용자의 업로드 상태별 파일 개수 조회
     */
    long countByUploadedByAndUploadStatusAndIsDeletedFalse(Long uploadedBy, UploadStatus status);
}

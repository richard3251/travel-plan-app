package com.travelapp.backend.domain.file.repository;

import com.travelapp.backend.domain.file.entity.TripImage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TripImageRepository extends JpaRepository<TripImage, Long> {

    /**
     * 특정 여행의 모든 완료된 이미지 조회 (표시 순서대로)
     */
    @Query("SELECT ti FROM TripImage ti WHERE ti.trip.id = :tripId " +
        "AND ti.fileInfo.uploadStatus = 'COMPLETED' AND ti.fileInfo.isDeleted = false " +
        "ORDER BY ti.displayOrder ASC")
    List<TripImage> findCompletedImagesByTripIdOrderByDisplayOrder(@Param("tripId") Long tripId);

    /**
     * 특정 여행의 커버 이미지 조회
     */
    @Query("SELECT ti FROM TripImage ti WHERE ti.trip.id = :tripId AND ti.isCoverImage = true " +
        "AND ti.fileInfo.uploadStatus = 'COMPLETED' AND ti.fileInfo.isDeleted = false")
    Optional<TripImage> findCoverImageByTripId(@Param("tripId") Long tripId);

    /**
     * 특정 여행의 일반 이미지들 조회 (커버 이미지 제외)
     */
    @Query("SELECT ti FROM TripImage ti WHERE ti.trip.id = :tripId AND ti.isCoverImage = false " +
        "AND ti.fileInfo.uploadStatus = 'COMPLETED' AND ti.fileInfo.isDeleted = false " +
        "ORDER BY ti.displayOrder ASC")
    List<TripImage> findNonCoverImagesByTripIdOrderByDisplayOrder(@Param("tripId") Long tripId);

    /**
     * 특정 여행의 완료된 이미지 개수 조회
     */
    @Query("SELECT COUNT(ti) FROM TripImage ti WHERE ti.trip.id = :tripId " +
        "AND ti.fileInfo.uploadStatus = 'COMPLETED' AND ti.fileInfo.isDeleted = false")
    long countCompletedImagesByTripId(@Param("tripId") Long tripId);

    /**
     * 특정 여행의 모든 커버 이미지를 일반 이미지로 변경
     */
    @Modifying
    @Query("UPDATE TripImage ti SET ti.isCoverImage = false WHERE ti.trip.id = :tripId")
    void clearCoverImages(@Param("tripId") Long tripId);

    /**
     * 특정 여행의 최대 표시 순서 조회
     */
    @Query("SELECT COALESCE(MAX(ti.displayOrder), 0) FROM TripImage ti WHERE ti.trip.id = :tripId " +
        "AND ti.fileInfo.uploadStatus = 'COMPLETED' AND ti.fileInfo.isDeleted = false")
    Integer getMaxDisplayOrder(@Param("tripId") Long tripId);

    /**
     * 특정 파일 ID로 여행 이미지 조회
     */
    Optional<TripImage> findByFileInfoId(Long fileInfoId);

    /**
     * 특정 여행의 특정 순서 이후 이미지들의 순서를 1씩 증가
     */
    @Modifying
    @Query("UPDATE TripImage ti SET ti.displayOrder = ti.displayOrder + 1 " +
        "WHERE ti.trip.id = :tripId AND ti.displayOrder >= :fromOrder " +
        "AND ti.fileInfo.uploadStatus = 'COMPLETED' AND ti.fileInfo.isDeleted = false")
    void incrementDisplayOrder(@Param("tripId") Long tripId, @Param("fromOrder") Integer fromOrder);

    /**
     * 특정 여행과 소유자로 이미지 조회 (권한 확인용)
     */
    @Query("SELECT ti FROM TripImage ti WHERE ti.id = :imageId AND ti.trip.member.id = :memberId " +
        "AND ti.fileInfo.uploadStatus = 'COMPLETED' AND ti.fileInfo.isDeleted = false")
    Optional<TripImage> findCompletedImageByIdAndTripOwnerId(@Param("imageId") Long imageId,
        @Param("memberId") Long memberId);
}

package com.travelapp.backend.domain.tripshare.repository;

import com.travelapp.backend.domain.tripshare.entity.TripShare;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TripShareRepository extends JpaRepository<TripShare, Long> {

    /**
     * 공유 토큰으로 공유 정보 조회
     */
    Optional<TripShare> findByShareToken(String shareToken);

    /**
     * 특정 여행의 공유 정보 조회
     */
    Optional<TripShare> findByTripId(Long tripId);

    /**
     * 특정 회원 여행들의 공유 정보들을 조회
     */
    @Query("SELECT ts FROM TripShare ts WHERE ts.trip.member.id = :memberId")
    List<TripShare> findByTripOwnerId(@Param("memberId") Long memberId);

    /**
     * 공개된 TripShare 정보들을 조회순으로 조회 (페이징)
     */
    @Query("SELECT ts"
        + "FROM TripShare ts"
        + "WHERE ts.isPublic = true AND (ts.expiryDate IS NULL OR ts.expiryDate > CURRENT_TIMESTAMP)"
        + "ORDER BY ts.viewCount DESC")
    Page<TripShare> findPublicTripShareByViewCount(Pageable pageable);

    /**
     *  공개된 TripShare 정보들을 최신순으로 조회 (페이징)
     */
    @Query("SELECT ts"
        + "FROM TripShare ts"
        + "WHERE ts.isPublic = true AND (ts.expiryDate IS NULL OR ts.expiryDate > CURRENT_TIMESTAMP)"
        + "ORDER BY ts.createdAt DESC")
    Page<TripShare> findPublicTripShareByCreatedAt(Pageable pageable);

    /**
     *  만료되지 않은 공유 토큰인지 확인
     */
    @Query("SELECT COUNT(ts) > 0"
        + "FROM TripShare ts"
        + "WHERE ts.shareToken = :shareToken AND ts.isPublic = true AND (ts.expiryDate IS NULL OR ts.expiryDate > CURRENT_TIMESTAMP)")
    boolean findPublicTripShareByShareToken(@Param("shareToken") String shareToken);

    /**
     * 특정 여행이 공유되어 있는지 확인
     */
    boolean existsByTripId(Long tripId);
}

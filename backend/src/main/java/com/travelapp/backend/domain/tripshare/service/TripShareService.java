package com.travelapp.backend.domain.tripshare.service;

import com.travelapp.backend.domain.trip.entity.Trip;
import com.travelapp.backend.domain.trip.service.TripService;
import com.travelapp.backend.domain.tripshare.dto.request.TripShareCreateRequest;
import com.travelapp.backend.domain.tripshare.dto.response.TripShareResponse;
import com.travelapp.backend.domain.tripshare.entity.TripShare;
import com.travelapp.backend.domain.tripshare.exception.TripShareAccessDeniedException;
import com.travelapp.backend.domain.tripshare.exception.TripShareNotFoundException;
import com.travelapp.backend.domain.tripshare.repository.TripShareRepository;
import com.travelapp.backend.global.exception.BusinessException;
import com.travelapp.backend.global.exception.dto.ErrorCode;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripShareService {

    private final TripShareRepository tripShareRepository;
    private final TripService tripService;

    @Value("${app.base-url:http://localhost:3000}")
    private String baseUrl;

    /**
     * 여행 공유 링크 생성
     */
    @Transactional
    public TripShareResponse createSharingLink(Long tripId, Long memberId,
        TripShareCreateRequest request) {
        log.info("여행 공유 링크 생성 요청 - 여행 ID: {}, 회원 ID: {}", tripId, memberId);

        // 권한 확인 - 본인의 여행인지 검증
        Trip trip = tripService.findTripWithOwnerValidation(tripId);

        // 이미 공유된 여행인지 확인
        if (tripShareRepository.existsByTripId(tripId)) {
            log.warn("이미 공유된 여행입니다 - 여행 ID: {}", tripId);
            throw new BusinessException(ErrorCode.TRIP_SHARE_ALREADY_EXISTS);
        }

        // 공유 토큰 생성 (UUID)
        String shareToken = UUID.randomUUID().toString();

        // 공유 정보 저장
        TripShare tripShare = TripShare.builder()
            .trip(trip)
            .shareToken(shareToken)
            .isPublic(request.getIsPublic())
            .expiryDate(request.getExpiryDate())
            .build();

        TripShare savedTripShare = tripShareRepository.save(tripShare);

        log.info("여행 공유 링크 생성 완료 - 공유 ID: {}, 토큰: {}", savedTripShare.getId(), shareToken);

        return TripShareResponse.of(savedTripShare, baseUrl);
    }

    /**
     * 공유 토큰으로 여행 조회
     */
    @Transactional
    public TripShareResponse getSharedTrip(String shareToken) {
        log.info("공유된 여행 조회 요청 - 토큰: {}", shareToken);

        TripShare tripShare = tripShareRepository.findByShareToken(shareToken)
            .orElseThrow(() -> {
                log.warn("존재하지 않는 공유 토큰 - 토큰: {}", shareToken);
                return new TripShareNotFoundException(shareToken);
            });

        // 접근 권한 확인
        validateTripShareAccess(tripShare);

        // 조회수 증가
        tripShare.incrementViewCount();
        tripShareRepository.save(tripShare);

        log.info("공유된 여행 조회 완료 - 여행 ID: {}, 조회수: {}", tripShare.getId(), tripShare.getViewCount());

        return TripShareResponse.of(tripShare, baseUrl);
    }

    /**
     * 내 여행들의 공유 정보들을 조회
     */
    @Transactional(readOnly = true)
    public List<TripShareResponse> getMySharedTrips(Long memberId) {
        log.info("내 여행들의 공유 정보들을 조회 - 회원 ID: {}", memberId);

        List<TripShare> tripShares = tripShareRepository.findByTripOwnerId(memberId);

        return tripShares.stream()
            .map(tripShare -> TripShareResponse.of(tripShare, baseUrl))
            .toList();
    }

    /**
     * 공개된 여행 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<TripShareResponse> getPublicSharedTrips(Pageable pageable, String sortBy) {
        log.info("공개 여행 목록 조회 - 정렬: {}, 페이지: {}", sortBy, pageable.getPageNumber());

        Page<TripShare> tripShares;

        if ("popular".equals(sortBy)) {
            tripShares = tripShareRepository.findPublicTripShareByViewCount(pageable);
        } else {
            tripShares = tripShareRepository.findPublicTripShareByCreatedAt(pageable);
        }

        return tripShares.map(tripShare -> TripShareResponse.of(tripShare, baseUrl));
    }

    /**
     * 여행 공유 설정 수정
     */
    @Transactional
    public TripShareResponse updateTripShare(Long tripId, Long memberId, TripShareCreateRequest request) {
        log.info("여행 공유 설정 수정 - 여행 ID: {}, 회원 ID: {}", tripId, memberId);

        // 권한 확인
        tripService.findTripWithOwnerValidation(tripId);

        TripShare tripShare = tripShareRepository.findByTripId(tripId)
            .orElseThrow(() -> new TripShareNotFoundException());

        // 설정 업데이트
        tripShare.updatePublicStatus(request.getIsPublic());
        tripShare.updateExpiryDate(request.getExpiryDate());

        TripShare updatedTripShare = tripShareRepository.save(tripShare);

        log.info("여행 공유 설정 수정 완료 - 공유 ID: {}", updatedTripShare.getId());

        return TripShareResponse.of(updatedTripShare, baseUrl);
    }

    /**
     * 여행 공유 삭제
     */
    @Transactional
    public void deleteTripShare(Long tripId, Long memberId) {
        log.info("여행 공유 삭제 - 여행 ID: {}, 회원 ID: {}", tripId, memberId);

        // 권한 확인
        tripService.findTripWithOwnerValidation(tripId);

        TripShare tripShare = tripShareRepository.findByTripId(tripId)
            .orElseThrow(() -> new TripShareNotFoundException());

        tripShareRepository.delete(tripShare);

        log.info("여행 공유 삭제 완료 - 공유 ID: {}", tripShare.getId());
    }

    /**
     * 공유된 여행 접근 권한 검증
     */
    private void validateTripShareAccess(TripShare tripShare) {
        // 비공개 여행인 경우
        if (!tripShare.getIsPublic()) {
            log.warn("비공개 여행에 접근 시도 - 공유 ID: {}", tripShare.getId());
            throw new TripShareAccessDeniedException("비공개 여행입니다");
        }

        // 만료된 여행인 경우
        if (tripShare.isExpired()) {
            log.warn("만료된 여행에 접근 시도 - 공유 ID: {}", tripShare.getId());
            throw new TripShareAccessDeniedException("만료된 공유 링크입니다");
        }
    }
}

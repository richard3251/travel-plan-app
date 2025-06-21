package com.travelapp.backend.domain.trip.service;

import com.travelapp.backend.domain.member.entity.Member;
import com.travelapp.backend.domain.member.exception.MemberNotFoundException;
import com.travelapp.backend.domain.member.repository.MemberRepository;
import com.travelapp.backend.domain.trip.dto.request.TripCreateRequest;
import com.travelapp.backend.domain.trip.dto.request.TripModifyRequest;
import com.travelapp.backend.domain.trip.dto.response.TripResponse;
import com.travelapp.backend.domain.trip.entity.Trip;
import com.travelapp.backend.domain.trip.exception.TripAccessDeniedException;
import com.travelapp.backend.domain.trip.exception.TripNotFoundException;
import com.travelapp.backend.domain.trip.repository.TripRepository;
import com.travelapp.backend.global.util.SecurityUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void createTrip(Long memberId, TripCreateRequest request) {

        Member member = memberRepository.findById(memberId).orElseThrow(
            MemberNotFoundException::new
        );

        Trip trip = Trip.builder()
            .member(member)
            .title(request.getTitle())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .region(request.getRegion())
            .regionLat(request.getLatitude())
            .regionLng(request.getLongitude())
            .build();

        tripRepository.save(trip);
    }

    @Transactional(readOnly = true)
    public List<TripResponse> getTrips(Long memberId) {

        return tripRepository.findByMember_Id(memberId).stream()
            .map(TripResponse::of)
            .toList();
    }

    @Transactional(readOnly = true)
    public TripResponse getTrip(Long tripId) {

        Trip trip = findTripWithOwnerValidation(tripId);

        return TripResponse.of(trip);
    }

    @Transactional
    public void modifyTrip(Long tripId, TripModifyRequest request) {

        Trip trip = findTripWithOwnerValidation(tripId);

        trip.modifyTrip(request);

        tripRepository.save(trip);
    }

    @Transactional
    public void deleteTrip(Long tripId) {

        Trip trip = findTripWithOwnerValidation(tripId);

        tripRepository.delete(trip);
    }

    /**
     * Trip 존재 여부 확인
     */
    private Trip existsTrip(Long tripId) {
        return tripRepository.findById(tripId).orElseThrow(
            () -> new TripNotFoundException(tripId)
        );
    }

    /**
     * 현재 사용자가 Trip의 소유자인지 확인
     */
    private void validateTripOwner(Trip trip) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        if (!trip.getMember().getId().equals(currentMemberId)) {
            throw new TripAccessDeniedException();
        }
    }

    /**
     * Trip 존재 및 소유자 확인 (다른 서비스에서 사용할 수 있도록 public)
     */
    public Trip findTripWithOwnerValidation(Long tripId) {
        Trip trip = existsTrip(tripId);
        validateTripOwner(trip);
        return trip;
    }


}

package com.travelapp.backend.domain.trip.repository;

import com.travelapp.backend.domain.trip.entity.Trip;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByMember_Id(Long memberId);

}

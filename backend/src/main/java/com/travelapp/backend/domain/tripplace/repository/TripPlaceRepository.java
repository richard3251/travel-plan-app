package com.travelapp.backend.domain.tripplace.repository;

import com.travelapp.backend.domain.tripplace.entity.TripPlace;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripPlaceRepository extends JpaRepository<TripPlace, Long> {

    List<TripPlace> findByTripDay_Id(Long tripDayId);

}

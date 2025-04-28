package com.travelapp.backend.domain.tripday.repository;

import com.travelapp.backend.domain.tripday.entity.TripDay;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripDayRepository extends JpaRepository<TripDay, Long> {

    List<TripDay> findByTrip_id(Long tripId);


}

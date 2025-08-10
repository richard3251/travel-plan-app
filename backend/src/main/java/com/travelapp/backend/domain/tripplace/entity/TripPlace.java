package com.travelapp.backend.domain.tripplace.entity;

import com.travelapp.backend.domain.tripday.entity.TripDay;
import com.travelapp.backend.domain.tripplace.dto.request.TripPlaceUpdateRequest;
import com.travelapp.backend.domain.tripplace.dto.request.VisitOrderUpdateRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "trip_place",
    indexes = {
        @Index(name = "idx_trip_place_trip_day_id", columnList = "trip_day_id")
    },
    uniqueConstraints = {
        @jakarta.persistence.UniqueConstraint(name = "uk_trip_place_trip_day_id_visit_order", columnNames = {"trip_day_id", "visit_order"})
    }
)
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class TripPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_day_id", nullable = false)
    private TripDay tripDay;

    private String placeId;

    @Column(nullable = false)
    private String placeName;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private String memo;

    @Column(nullable = false)
    private LocalTime visitTime;

    @Column(nullable = false)
    private Integer visitOrder;

    public void update(TripPlaceUpdateRequest request) {
        this.placeName = request.getPlaceName();
        this.address = request.getAddress();
        this.latitude = request.getLatitude();
        this.longitude = request.getLongitude();
        this.memo = request.getMemo();
        this.placeId = request.getPlaceId();
        this.visitTime = request.getVisitTime();
        this.visitOrder = request.getVisitOrder();
    }

    public void visitOrderUpdate(VisitOrderUpdateRequest request) {
        this.visitOrder = request.getVisitOrder();
    }

}

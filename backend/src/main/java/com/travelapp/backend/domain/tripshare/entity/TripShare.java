package com.travelapp.backend.domain.tripshare.entity;

import com.travelapp.backend.domain.trip.entity.Trip;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "trip_share")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class TripShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(name = "share_token", nullable = false, unique = true, length = 36)
    private String shareToken;

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = true;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    public boolean isExpired() {
        return expiryDate != null && LocalDateTime.now().isAfter(expiryDate);
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void updatePublicStatus(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public void updateExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }
}

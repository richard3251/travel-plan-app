package com.travelapp.backend.domain.chat.entity;

import com.travelapp.backend.domain.trip.entity.Trip;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_room")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 여행 (1:1 관계)
     * 한 여행당 하나의 채팅방
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false, unique = true)
    private Trip trip;

    /**
     * 채팅방 이름 (선택사항, 기본적으로 여행 제목 사용)
     */
    @Column(length = 100)
    private String name;

    /**
     * 생성 일시
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 마지막 메시지 시간 (채팅방 정렬용)
     */
    @Column
    private LocalDateTime lastMessageAt;

    /**
     * 활성화 여부
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * 마지막 메시지 업데이트
     */
    public void updateLastMessageTime() {
        this.lastMessageAt = LocalDateTime.now();
    }
}

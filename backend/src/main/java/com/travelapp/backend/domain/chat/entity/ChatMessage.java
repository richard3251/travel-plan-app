package com.travelapp.backend.domain.chat.entity;

import com.travelapp.backend.domain.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.awt.TrayIcon.MessageType;
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
@Table(name = "chat_message", indexes = {
    @Index(name = "idx_chat_room_created", columnList = "chat_room_id, created_at")
})
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Member member;

    /**
     * 메시지 타입
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageType messageType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * 이미지 URL(타입이 IMAGE인 경우)
     */
    @Column(length = 500)
    private String imageUrl;

    /**
     * 위치 정보 - 경도 (타입이 LOCATION인 경우)
     */
    @Column
    private Double latitude;

    /**
     * 위치 정보 - 위도 (타입이 LOCATION인 경우)
     */
    @Column
    private Double longitude;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 읽은 사람 수 (비정규화 - 성능 최적화)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer readCount = 0;

    /**
     * 삭제 여부
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    /**
     * 읽은 사람 수 증가
     */
    public void incrementReadCount() {
        this.readCount++;
    }

    public void delete() {
        this.deleted = true;
        this.content = "삭제된 메시지입니다.";
    }

}

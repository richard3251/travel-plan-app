package com.travelapp.backend.domain.member.entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("refreshToken")
public class RefreshToken {

    @Id
    private String token;

    private Long memberId;

    @TimeToLive
    private Long expiration;

    public static RefreshToken of(String token, Long memberId, Long expiration) {
        return RefreshToken.builder()
            .token(token)
            .memberId(memberId)
            .expiration(expiration)
            .build();
    }


}

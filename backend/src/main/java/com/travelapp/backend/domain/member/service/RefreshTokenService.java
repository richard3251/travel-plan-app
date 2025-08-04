package com.travelapp.backend.domain.member.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token";
    private static final long REFRESH_TOKEN_EXPIRATION_DAYS = 7;

    /**
     * Refresh Token을 Redis에 저장
     */
    public void saveRefreshToken(String token, Long memberId) {
        String key = REFRESH_TOKEN_PREFIX + token;
        String value = String.valueOf(memberId);

        redisTemplate.opsForValue().set(
            key,
            value,
            REFRESH_TOKEN_EXPIRATION_DAYS,
            TimeUnit.DAYS
        );

        log.info("Refresh Token 저장 완료 - 사용자 ID: {}", memberId);
    }

    /**
     * Refresh Token으로 사용자 ID 조회
     */
    public Long getMemberIdByToken(String token) {
        String key = REFRESH_TOKEN_PREFIX + token;
        String memberIdStr = redisTemplate.opsForValue().get(key);

        if (memberIdStr == null) {
            log.warn("유효하지 않은 Refresh Token: {}", token);
            return null;
        }

        try {
            return Long.parseLong(memberIdStr);
        } catch (NumberFormatException e) {
            log.error("Refresh Token에서 사용자 ID 파싱 실패: {}", memberIdStr);
            return null;
        }
    }

    /**
     * Refresh Token 존재 여부 확인
     */
    public boolean existsRefreshToken(String token) {
        String key = REFRESH_TOKEN_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Refresh Token 삭제 (로그아웃 시)
     */
    public void deleteRefreshToken(String token) {
        String key = REFRESH_TOKEN_PREFIX + token;
        Boolean deleted = redisTemplate.delete(key);

        if (Boolean.TRUE.equals(deleted)) {
            log.info("Refresh Token 삭제 완료: {}", token);
        } else {
            log.warn("삭제할 Refresh Token을 찾을 수 없음: {}", token);
        }
    }

    /**
     * 사용자의 모든 Refresh Token 삭제
     */
    public void deleteAllRefreshTokensByMemberId(Long memberId) {
        String pattern = REFRESH_TOKEN_PREFIX + "*";
        var keys = redisTemplate.keys(pattern);

        if (keys != null) {
            for (String key : keys) {
                String storedMemberId = redisTemplate.opsForValue().get(key);
                if (String.valueOf(memberId).equals(storedMemberId)) {
                    redisTemplate.delete(key);
                    log.info("사용자 {}의 Refresh Token 삭제: {}", memberId, key);
                }
            }
        }
    }

    /**
     * Refresh Token의 남은 만료 시간 조회 (초 단위)
     */
    public Long getTokenExpiration(String token) {
        String key = REFRESH_TOKEN_PREFIX + token;
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

}

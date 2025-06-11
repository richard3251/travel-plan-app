package com.travelapp.backend.global.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public class SecurityUtil {

    private SecurityUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 현재 인증된 사용자의 ID를 반환
     * @return 사용자 ID
     * @throws IllegalStateException 인증되지 않은 경우
     */
    public static Long getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
        "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("인증되지 않은 사용자입니다.");
        }

        try {
            return (Long) authentication.getPrincipal();
        } catch (ClassCastException e) {
            log.error("Authentication principal이 Long 타입이 아닙니다: {}", authentication.getPrincipal());
            throw new IllegalStateException("잘못된 인증 정보입니다.");
        }
    }

    /**
     * 현재 인증된 사용자의 ID를 Optional로 반환 (인증되지 않은 경우 null 반환)
     * @return 사용자 ID 또는 null
     */
    public static Long getCurrentMemberIdOrNull() {
        try {
            return getCurrentMemberId();
        } catch (IllegalStateException e) {
            log.debug("인증되지 않은 사용자 요청: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 현재 사용자가 인증되었는지 확인
     * @return 인증 여부
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return authentication != null && authentication.isAuthenticated() &&
            !"anonymousUser".equals(authentication.getPrincipal());
    }













}

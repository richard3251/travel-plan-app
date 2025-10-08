package com.travelapp.backend.global.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
public class CookieUtil {

    private CookieUtil() {
        throw new IllegalStateException("Utility class");
    }

    // 쿠키 이름 상수
    public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    /**
     * HTTP-Only 쿠키 생성
     *
     * @param name     쿠키 이름
     * @param value    쿠키 값
     * @param maxAge   만료 시간 (초)
     * @param isSecure HTTPS에서만 전송할지 여부
     * @return 생성된 쿠키
     */
    public static Cookie createCookie(String name, String value, int maxAge, boolean isSecure) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);           // javaScript 접근 차단
        cookie.setSecure(isSecure);         // HTTPS에서만 전송 (개발환경에서는 false)
        cookie.setPath("/");                // 모든 경로에서 접근 가능
        cookie.setMaxAge(maxAge);           // 만료 시간 설정
        // 개발 환경에서는 SameSite=Lax로 설정 (CSRF 방지하면서 일반적인 요청 허용)
        cookie.setAttribute("SameSite", "Lax");

        log.debug("쿠키 생성: name={}, maxAge = {}, secure = {}, SameSite=Lax", name, maxAge, isSecure);
        return cookie;
    }

    /**
     * Access Token 쿠키 생성 (24시간)
     */
    public static Cookie createAccessTokenCookie(String token, boolean isSecure) {
        return createCookie(ACCESS_TOKEN_COOKIE_NAME, token, 24 * 60 * 60, isSecure);
    }

    /**
     * Refresh Token 쿠키 생성 (7일)
     */
    public static Cookie createRefreshTokenCookie(String token, boolean isSecure) {
        return createCookie(REFRESH_TOKEN_COOKIE_NAME, token, 7 * 24 * 60 * 60, isSecure);
    }

    /**
     * 쿠키 무효화 (로그아웃 시 사용)
     */
    public static Cookie createExpiredCookie(String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 개발환경 고려
        cookie.setPath("/");
        cookie.setMaxAge(0); // 즉시 만료
        cookie.setAttribute("SameSite", "Lax");

        log.debug("쿠키 무효화: name={}", name);
        return cookie;
    }

    /**
     * 요청에서 특정 쿠키 값 추출
     */
    public static String getCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    String value = cookie.getValue();
                    log.debug("쿠키에서 값 추출: name={}, hasValue={}", cookieName, StringUtils.hasText(value));
                    return value;
                }
            }
        }

        log.debug("쿠키에서 값을 찾을 수 없음: name={}", cookieName);
        return null;
    }

    /**
     * Access Token 쿠키에서 값 추출
     */
    public static String getAccessTokenFromCookie(HttpServletRequest request) {
        return getCookieValue(request, ACCESS_TOKEN_COOKIE_NAME);
    }

    /**
     * Refresh Token 쿠키에서 값 추출
     */
    public static String getRefreshTokenFromCookie(HttpServletRequest request) {
        return getCookieValue(request, REFRESH_TOKEN_COOKIE_NAME);
    }

    /**
     * 모든 토큰 쿠키 무효화
     */
    public static void clearAllTokenCookies(HttpServletResponse response) {
        response.addCookie(createExpiredCookie(ACCESS_TOKEN_COOKIE_NAME));
        response.addCookie(createExpiredCookie(REFRESH_TOKEN_COOKIE_NAME));
        log.info("모든 토큰 쿠키 무효화 완료");
    }

}

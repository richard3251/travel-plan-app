package com.travelapp.backend.domain.member.service;

import com.travelapp.backend.domain.member.dto.request.MemberLoginRequest;
import com.travelapp.backend.domain.member.dto.response.MemberLoginResponse;
import com.travelapp.backend.domain.member.dto.response.MemberResponse;
import com.travelapp.backend.domain.member.dto.response.TokenRefreshResponse;
import com.travelapp.backend.global.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final MemberService memberService;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    /**
     * 로그인 처리 및 쿠키 설정
     */
    @Transactional
    public MemberResponse loginWithCookie(MemberLoginRequest request, HttpServletResponse response) {
        // 로그인 처리
        MemberLoginResponse loginResponse = memberService.login(request);

        // 쿠키 설정
        setTokenCookies(response, loginResponse.getAccessToken(), loginResponse.getRefreshToken());

        log.info("사용자 로그인 성공 - ID: {}, 쿠키 설정 완료", loginResponse.getId());

        // 사용자 정보 반환 (토큰 정보 제외)
        return memberService.getMemberById(loginResponse.getId());
    }

    /**
     * 토큰 갱신 처리 및 쿠키 설정
     */
    @Transactional
    public void refreshTokenWithCookie(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 Refresh Token 추출
        String refreshToken = CookieUtil.getRefreshTokenFromCookie(request);

        if (!StringUtils.hasText(refreshToken)) {
            log.warn("토큰 갱신 실패 - Refresh Token이 쿠키에 없음");
            throw new IllegalArgumentException("Refresh Token이 없습니다."); // TODO: 예외처리 커스터마이징
        }

        // 토큰 갱신
        TokenRefreshResponse refreshResponse = memberService.refreshToken(refreshToken);

        // 새로운 토큰들을 쿠키로 설정
        setTokenCookies(response, refreshResponse.getAccessToken(), refreshResponse.getRefreshToken());

        log.info("토큰 갱신 성공 - 새로운 쿠키 설정 완료");
    }

    /**
     * 로그아웃 처리 및 쿠키 무효화
     */
    public void logoutWithCookie(HttpServletResponse response) {
        CookieUtil.clearAllTokenCookies(response);
        log.info("사용자 로그아웃 완료 - 쿠키 무효화");
    }

    /**
     * 토큰 쿠키 설정 (내부 메서드)
     */
    private void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        boolean isSecure = isProductionEnvironment();

        response.addCookie(CookieUtil.createAccessTokenCookie(accessToken, isSecure));
        response.addCookie(CookieUtil.createRefreshTokenCookie(refreshToken, isSecure));

        log.debug("토큰 쿠키 설정 완료 (secure={})", isSecure);
    }

    /**
     * 프로덕션 환경 여부 확인
     */
    private boolean isProductionEnvironment() {
        return "prod".equals(activeProfile);
    }
    
}

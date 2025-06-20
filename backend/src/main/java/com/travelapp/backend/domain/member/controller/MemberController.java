package com.travelapp.backend.domain.member.controller;

import com.travelapp.backend.domain.member.dto.request.MemberLoginRequest;
import com.travelapp.backend.domain.member.dto.request.MemberSignUpRequest;
import com.travelapp.backend.domain.member.dto.request.TokenRefreshRequest;
import com.travelapp.backend.domain.member.dto.response.MemberLoginResponse;
import com.travelapp.backend.domain.member.dto.response.MemberResponse;
import com.travelapp.backend.domain.member.dto.response.TokenRefreshResponse;
import com.travelapp.backend.domain.member.service.MemberService;
import com.travelapp.backend.global.util.CookieUtil;
import com.travelapp.backend.global.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @PostMapping("/signup")
    public ResponseEntity<MemberResponse> signup(
       @Valid @RequestBody MemberSignUpRequest request
    ) {
        MemberResponse response = memberService.signUp(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<MemberResponse> login(
        @Valid @RequestBody MemberLoginRequest request,
        HttpServletResponse response
    ) {
        MemberLoginResponse loginResponse = memberService.login(request);

        // 개발 환경에서는 secure=false, 프로덕션에서는 secure=true
        boolean isSecure = "prod".equals(activeProfile);

        response.addCookie(CookieUtil.createAccessTokenCookie(loginResponse.getAccessToken(), isSecure));
        response.addCookie(CookieUtil.createRefreshTokenCookie(loginResponse.getRefreshToken(), isSecure));

        log.info("사용자 로그인 성공 - ID: {}, 쿠키 설정 완료 (secure={})", loginResponse.getId(), isSecure);

        MemberResponse memberResponse = memberService.getMemberById(loginResponse.getId());

        return ResponseEntity.ok(memberResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refreshToken(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        // 쿠키에서 Refresh Token 추출
        String refreshToken = CookieUtil.getRefreshTokenFromCookie(request);

        if (!StringUtils.hasText(refreshToken)) {
            log.warn("토큰 갱신 실패 - Refresh Token이 쿠키에 없음");
            return ResponseEntity.badRequest().body("Refresh Token이 없습니다.");
        }

        // TokenRefreshRequest 객체 생성
        TokenRefreshRequest refreshRequest = TokenRefreshRequest.builder()
            .refreshToken(refreshToken)
            .build();

        TokenRefreshResponse refreshResponse = memberService.refreshToken(refreshRequest);

        // 개발환경에서는 secure=false, 프로덕션에서는 secure=true
        boolean isSecure = "prod".equals(activeProfile);

        // 새로운 토큰들을 쿠키로 설정
        response.addCookie(CookieUtil.createAccessTokenCookie(refreshResponse.getAccessToken(), isSecure));
        response.addCookie(CookieUtil.createRefreshTokenCookie(refreshResponse.getRefreshToken(), isSecure));

        log.info("토큰 갱신 성공 - 새로운 쿠키 설정 완료 (secure={})", isSecure);

        return ResponseEntity.ok("토큰이 성공적으로 갱신되었습니다.");
    }

    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getCurrentMember() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        MemberResponse response = memberService.getMemberById(memberId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        // 모든 토큰 쿠키 무효화
        CookieUtil.clearAllTokenCookies(response);

        log.info("사용자 로그아웃 완료 - 쿠키 무효화");

        return ResponseEntity.ok("로그아웃이 완료되었습니다.");
    }

}

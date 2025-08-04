package com.travelapp.backend.domain.member.controller;

import com.travelapp.backend.domain.member.dto.request.MemberLoginRequest;
import com.travelapp.backend.domain.member.dto.request.MemberSignUpRequest;
import com.travelapp.backend.domain.member.dto.response.MemberResponse;
import com.travelapp.backend.domain.member.service.AuthenticationService;
import com.travelapp.backend.domain.member.service.MemberService;
import com.travelapp.backend.global.util.CookieUtil;
import com.travelapp.backend.global.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    private final AuthenticationService authenticationService;

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
        MemberResponse memberResponse = authenticationService.loginWithCookie(request, response);

        return ResponseEntity.ok(memberResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refreshToken(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        authenticationService.refreshTokenWithCookie(request, response);

        return ResponseEntity.ok("토큰이 성공적으로 갱신되었습니다.");
    }

    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getCurrentMember() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        MemberResponse response = memberService.getMemberById(memberId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        authenticationService.logoutWithCookie(request, response);

        return ResponseEntity.ok("로그아웃이 완료되었습니다.");
    }

    @PostMapping("/logout-all")
    public ResponseEntity<String> logoutFromAllDevices(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        // 모든 기기에서 로그아웃
        memberService.logoutFromAllDevices(memberId);

        //현재 기기 쿠키도 무효화
        CookieUtil.clearAllTokenCookies(response);

        return ResponseEntity.ok("모든 기기에서 로그아웃이 완료되었습니다.");
    }

}

package com.travelapp.backend.domain.member.controller;

import com.travelapp.backend.domain.member.dto.request.MemberLoginRequest;
import com.travelapp.backend.domain.member.dto.request.MemberSignUpRequest;
import com.travelapp.backend.domain.member.dto.response.MemberResponse;
import com.travelapp.backend.domain.member.service.AuthenticationService;
import com.travelapp.backend.domain.member.service.MemberService;
import com.travelapp.backend.global.util.CookieUtil;
import com.travelapp.backend.global.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "회원 관리", description = "회원가입, 로그인, 로그아웃 및 인증 관련 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;
    private final AuthenticationService authenticationService;

    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "회원가입 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")
    })
    @PostMapping("/signup")
    public ResponseEntity<MemberResponse> signup(
       @Parameter(description = "회원가입 요청 정보") @Valid @RequestBody MemberSignUpRequest request
    ) {
        MemberResponse response = memberService.signUp(request);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다. 성공 시 JWT 토큰이 쿠키에 설정됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/login")
    public ResponseEntity<MemberResponse> login(
        @Parameter(description = "로그인 요청 정보") @Valid @RequestBody MemberLoginRequest request,
        @Parameter(hidden = true) HttpServletResponse response
    ) {
        MemberResponse memberResponse = authenticationService.loginWithCookie(request, response);

        return ResponseEntity.ok(memberResponse);
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 Access Token을 갱신합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshToken(
        @Parameter(hidden = true) HttpServletRequest request,
        @Parameter(hidden = true) HttpServletResponse response
    ) {
        authenticationService.refreshTokenWithCookie(request, response);

        return ResponseEntity.ok("토큰이 성공적으로 갱신되었습니다.");
    }

    @Operation(summary = "현재 사용자 정보 조회", description = "로그인한 사용자의 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getCurrentMember() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        MemberResponse response = memberService.getMemberById(memberId);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로그아웃", description = "현재 기기에서 로그아웃합니다. Refresh Token이 무효화됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
        @Parameter(hidden = true) HttpServletRequest request,
        @Parameter(hidden = true) HttpServletResponse response
    ) {
        authenticationService.logoutWithCookie(request, response);

        return ResponseEntity.ok("로그아웃이 완료되었습니다.");
    }

    @Operation(summary = "모든 기기에서 로그아웃", description = "사용자의 모든 기기에서 로그아웃합니다. 모든 Refresh Token이 무효화됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "모든 기기에서 로그아웃 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping("/logout-all")
    public ResponseEntity<String> logoutFromAllDevices(
        @Parameter(hidden = true) HttpServletResponse response
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        // 모든 기기에서 로그아웃
        memberService.logoutFromAllDevices(memberId);

        //현재 기기 쿠키도 무효화
        CookieUtil.clearAllTokenCookies(response);

        return ResponseEntity.ok("모든 기기에서 로그아웃이 완료되었습니다.");
    }

}

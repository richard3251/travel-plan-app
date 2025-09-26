package com.travelapp.backend.domain.member.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.travelapp.backend.domain.member.dto.request.MemberLoginRequest;
import com.travelapp.backend.domain.member.dto.response.MemberLoginResponse;
import com.travelapp.backend.domain.member.dto.response.MemberResponse;
import com.travelapp.backend.domain.member.dto.response.TokenRefreshResponse;
import com.travelapp.backend.domain.member.entity.Member;
import com.travelapp.backend.domain.member.entity.Role;
import com.travelapp.backend.global.exception.InvalidValueException;
import com.travelapp.backend.global.exception.dto.ErrorCode;
import com.travelapp.backend.global.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService 테스트")
public class AuthenticationServiceTest {

    @Mock
    private MemberService memberService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthenticationService authenticationService;

    private MemberLoginRequest loginRequest;
    private MemberLoginResponse loginResponse;
    private MemberResponse memberResponse;
    private TokenRefreshResponse tokenRefreshResponse;
    private Member testMember;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        testMember = Member.builder()
            .id(1L)
            .email("test@example.com")
            .nickname("testUser")
            .password("encodedPassword")
            .role(Role.USER)
            .build();

        loginRequest = MemberLoginRequest.builder()
            .email("test@example.com")
            .password("password123!")
            .build();

        loginResponse = MemberLoginResponse.builder()
            .id(1L)
            .email("test@example.com")
            .nickname("testUser")
            .accessToken("access.token.value")
            .refreshToken("refresh.token.value")
            .build();

        memberResponse = MemberResponse.builder()
            .id(1L)
            .email("test@example.com")
            .nickname("testUser")
            .role(Role.USER)
            .build();

        tokenRefreshResponse = TokenRefreshResponse.builder()
            .accessToken("new.access.token")
            .refreshToken("new.refresh.token")
            .build();

        // activeProfile 기본값 설정 (dev 환경)
        ReflectionTestUtils.setField(authenticationService, "activeProfile", "dev");
    }

    @Nested
    @DisplayName("쿠키 기반 로그인")
    class LoginWithCookie {

        @Test
        @DisplayName("성공 - 유효한 로그인 요청으로 쿠키 설정")
        void loginWithCookie_Success() {
            // given
            given(memberService.login(loginRequest)).willReturn(loginResponse);
            given(memberService.getMemberById(1L)).willReturn(memberResponse);

            try (MockedStatic<CookieUtil> cookieUtilMock = Mockito.mockStatic(CookieUtil.class)) {
                Cookie accessTokenCookie = new Cookie("accessToken", "access.token.value");
                Cookie refreshTokenCookie = new Cookie("refreshToken", "refresh.token.value");

                cookieUtilMock.when(
                        () -> CookieUtil.createAccessTokenCookie("access.token.value", false))
                    .thenReturn(accessTokenCookie);
                cookieUtilMock.when(
                        () -> CookieUtil.createRefreshTokenCookie("refresh.token.value", false))
                    .thenReturn(refreshTokenCookie);

                // when
                MemberResponse result = authenticationService.loginWithCookie(loginRequest,
                    response);

                // then
                assertThat(result).isNotNull();
                assertThat(result.getId()).isEqualTo(1L);
                assertThat(result.getEmail()).isEqualTo("test@example.com");
                assertThat(result.getNickname()).isEqualTo("testUser");

                // 서비스 호출 검증
                verify(memberService, times(1)).login(loginRequest);
                verify(memberService, times(1)).getMemberById(1L);

                // 쿠키 설정 검증
                verify(response, times(1)).addCookie(accessTokenCookie);
                verify(response, times(1)).addCookie(refreshTokenCookie);

                // CookieUtil 호출 검증
                cookieUtilMock.verify(
                    () -> CookieUtil.createAccessTokenCookie("access.token.value", false),
                    times(1));
                cookieUtilMock.verify(
                    () -> CookieUtil.createRefreshTokenCookie("refresh.token.value", false),
                    times(1));
            }
        }

        @Test
        @DisplayName("성공 - 프로덕션 환경에서 secure 쿠키 설정")
        void loginWithCookie_Success_ProductionEnvironment() {
            // given
            ReflectionTestUtils.setField(authenticationService, "activeProfile", "prod");
            given(memberService.login(loginRequest)).willReturn(loginResponse);
            given(memberService.getMemberById(1L)).willReturn(memberResponse);

            try (MockedStatic<CookieUtil> cookieUtilMock = Mockito.mockStatic(CookieUtil.class)) {
                Cookie accessTokenCookie = new Cookie("accessToken", "access.token.value");
                Cookie refreshTokenCookie = new Cookie("refreshToken", "refresh.token.value");

                cookieUtilMock.when(
                        () -> CookieUtil.createAccessTokenCookie("access.token.value", true))
                    .thenReturn(accessTokenCookie);
                cookieUtilMock.when(
                        () -> CookieUtil.createRefreshTokenCookie("refresh.token.value", true))
                    .thenReturn(refreshTokenCookie);

                // when
                MemberResponse result = authenticationService.loginWithCookie(loginRequest,
                    response);

                // then
                assertThat(result).isNotNull();

                // Secure 쿠키 설정 검증 (운영 환경)
                cookieUtilMock.verify(
                    () -> CookieUtil.createAccessTokenCookie("access.token.value", true), times(1));
                cookieUtilMock.verify(
                    () -> CookieUtil.createRefreshTokenCookie("refresh.token.value", true),
                    times(1));
            }
        }
    }

    @Nested
    @DisplayName("토큰 갱신")
    class RefreshTokenWithCookie {

        @Test
        @DisplayName("성공 - 유효한 리프레시 토큰으로 토큰 갱신")
        void refreshTokenWithCookie_Success() {
            // given
            String refreshToken = "valid.refresh.token";

            try (MockedStatic<CookieUtil> cookieUtilMock = Mockito.mockStatic(CookieUtil.class)) {
                cookieUtilMock.when(() -> CookieUtil.getRefreshTokenFromCookie(request))
                    .thenReturn(refreshToken);

                given(memberService.refreshToken(refreshToken)).willReturn(tokenRefreshResponse);

                Cookie newAccessTokenCookie = new Cookie("accessToken", "new.access.token");
                Cookie newRefreshTokenCookie = new Cookie("refreshToken", "new.refresh.token");

                cookieUtilMock.when(
                        () -> CookieUtil.createAccessTokenCookie("new.access.token", false))
                    .thenReturn(newAccessTokenCookie);
                cookieUtilMock.when(
                        () -> CookieUtil.createRefreshTokenCookie("new.refresh.token", false))
                    .thenReturn(newRefreshTokenCookie);

                // when
                authenticationService.refreshTokenWithCookie(request, response);

                // then
                verify(memberService, times(1)).refreshToken(refreshToken);
                verify(response, times(1)).addCookie(newAccessTokenCookie);
                verify(response, times(1)).addCookie(newRefreshTokenCookie);

                cookieUtilMock.verify(() -> CookieUtil.getRefreshTokenFromCookie(request),
                    times(1));
                cookieUtilMock.verify(
                    () -> CookieUtil.createAccessTokenCookie("new.access.token", false), times(1));
                cookieUtilMock.verify(
                    () -> CookieUtil.createRefreshTokenCookie("new.refresh.token", false),
                    times(1));
            }
        }

        @Test
        @DisplayName("실패 - 쿠키에 리프레시 토큰이 없음")
        void refreshTokenWithCookie_Fail_NoRefreshTokenInCookie() {
            // given
            try (MockedStatic<CookieUtil> cookieUtilMock = Mockito.mockStatic(CookieUtil.class)) {
                cookieUtilMock.when(() -> CookieUtil.getRefreshTokenFromCookie(request))
                    .thenReturn(null);

                // when & then
                assertThatThrownBy(
                    () -> authenticationService.refreshTokenWithCookie(request, response))
                    .isInstanceOf(InvalidValueException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN);

                cookieUtilMock.verify(() -> CookieUtil.getRefreshTokenFromCookie(request),
                    times(1));
                verify(memberService, times(0)).refreshToken(anyString());
            }
        }

        @Test
        @DisplayName("실패 - 빈 문자열 리프레시 토큰")
        void refreshTokenWithCookie_Fail_EmptyRefreshToken() {
            // given
            try (MockedStatic<CookieUtil> cookieUtilMock = Mockito.mockStatic(CookieUtil.class)) {
                cookieUtilMock.when(() -> CookieUtil.getRefreshTokenFromCookie(request))
                    .thenReturn("");

                // when & then
                assertThatThrownBy(
                    () -> authenticationService.refreshTokenWithCookie(request, response))
                    .isInstanceOf(InvalidValueException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN);

                cookieUtilMock.verify(() -> CookieUtil.getRefreshTokenFromCookie(request),
                    times(1));
                verify(memberService, times(0)).refreshToken(anyString());
            }
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 리프레시 토큰")
        void refreshTokenWithCookie_Fail_InvalidRefreshToken() {
            // given
            String invalidRefreshToken = "invalid.refresh.token";

            try (MockedStatic<CookieUtil> cookieUtilMock = Mockito.mockStatic(
                CookieUtil.class)) {
                cookieUtilMock.when(() -> CookieUtil.getRefreshTokenFromCookie(request))
                    .thenReturn(invalidRefreshToken);

                given(memberService.refreshToken(invalidRefreshToken))
                    .willThrow(new InvalidValueException(ErrorCode.INVALID_TOKEN));

                // when & then
                assertThatThrownBy(
                    () -> authenticationService.refreshTokenWithCookie(request, response))
                    .isInstanceOf(InvalidValueException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN);

                verify(memberService, times(1)).refreshToken(invalidRefreshToken);
                verify(response, times(0)).addCookie(any(Cookie.class));
            }
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class LogoutWithCookie {

        @Test
        @DisplayName("성공 - 유효한 리프레시 토큰으로 로그아웃")
        void logoutWithCookie_Success_WithValidRefreshToken() {
            // given
            String refreshToken = "valid.refresh.token";

            try (MockedStatic<CookieUtil> cookieUtilMock = Mockito.mockStatic(CookieUtil.class)) {
                cookieUtilMock.when(() -> CookieUtil.getRefreshTokenFromCookie(request))
                    .thenReturn(refreshToken);

                doNothing().when(memberService).logout(refreshToken);

                // when
                authenticationService.logoutWithCookie(request, response);

                // then
                verify(memberService, times(1)).logout(refreshToken);
                cookieUtilMock.verify(() -> CookieUtil.getRefreshTokenFromCookie(request), times(1));
                cookieUtilMock.verify(() -> CookieUtil.clearAllTokenCookies(response), times(1));
            }
        }

        @Test
        @DisplayName("성공 - 리프레시 토큰이 없어도 쿠키 무효화")
        void logoutWithCookie_Success_WithoutRefreshToken() {
            // given
            try (MockedStatic<CookieUtil> cookieUtilMock = Mockito.mockStatic(CookieUtil.class)) {
                cookieUtilMock.when(() -> CookieUtil.getRefreshTokenFromCookie(request))
                    .thenReturn(null);

                // when
                authenticationService.logoutWithCookie(request, response);

                // then
                verify(memberService, times(0)).logout(anyString());
                cookieUtilMock.verify(() -> CookieUtil.getRefreshTokenFromCookie(request), times(1));
                cookieUtilMock.verify(() -> CookieUtil.clearAllTokenCookies(response), times(1));
            }
        }

        @Test
        @DisplayName("성공 - 빈 문자열 리프레시 토큰으로 로그아웃")
        void logoutWithCookie_Success_WithEmptyRefreshToken() {
            // given
            try (MockedStatic<CookieUtil> cookieUtilMock = Mockito.mockStatic(CookieUtil.class)) {
                cookieUtilMock.when(() -> CookieUtil.getRefreshTokenFromCookie(request))
                    .thenReturn("");

                // when
                authenticationService.logoutWithCookie(request, response);

                // then
                verify(memberService, times(0)).logout(anyString());
                cookieUtilMock.verify(() -> CookieUtil.getRefreshTokenFromCookie(request), times(1));
                cookieUtilMock.verify(() -> CookieUtil.clearAllTokenCookies(response), times(1));
            }

        }
    }

    @Nested
    @DisplayName("환경별 보안 설정")
    class EnvironmentSecuritySettings {

        @Test
        @DisplayName("개발 환경 - secure=false 쿠키 설정")
        void devEnvironment_SecureFalse() {
            // given
            ReflectionTestUtils.setField(authenticationService, "activeProfile", "dev");
            given(memberService.login(loginRequest)).willReturn(loginResponse);
            given(memberService.getMemberById(1L)).willReturn(memberResponse);

            try (MockedStatic<CookieUtil> cookieUtilMock = Mockito.mockStatic(CookieUtil.class)) {
                Cookie accessTokenCookie = new Cookie("accessToken", "access.token.value");
                Cookie refreshTokenCookie = new Cookie("refreshToken", "refresh.token.value");

                cookieUtilMock.when(
                        () -> CookieUtil.createAccessTokenCookie("access.token.value", false))
                    .thenReturn(accessTokenCookie);
                cookieUtilMock.when(
                        () -> CookieUtil.createRefreshTokenCookie("refresh.token.value", false))
                    .thenReturn(refreshTokenCookie);

                // when
                authenticationService.loginWithCookie(loginRequest, response);

                // then
                cookieUtilMock.verify(
                    () -> CookieUtil.createAccessTokenCookie("access.token.value", false),
                    times(1));
                cookieUtilMock.verify(
                    () -> CookieUtil.createRefreshTokenCookie("refresh.token.value", false),
                    times(1));
            }
        }

        @Test
        @DisplayName("프로덕션 환경 - secure=true 쿠키 설정")
        void prodEnvironment_SecureTrue() {
            // given
            ReflectionTestUtils.setField(authenticationService, "activeProfile", "prod");
            given(memberService.login(loginRequest)).willReturn(loginResponse);
            given(memberService.getMemberById(1L)).willReturn(memberResponse);

            try (MockedStatic<CookieUtil> cookieUtilMock = Mockito.mockStatic(CookieUtil.class)) {
                Cookie accessTokenCookie = new Cookie("accessToken", "access.token.value");
                Cookie refreshTokenCookie = new Cookie("refreshToken", "refresh.token.value");

                cookieUtilMock.when(() -> CookieUtil.createAccessTokenCookie("access.token.value", true))
                    .thenReturn(accessTokenCookie);
                cookieUtilMock.when(() -> CookieUtil.createRefreshTokenCookie("refresh.token.value", true))
                    .thenReturn(refreshTokenCookie);

                // when
                authenticationService.loginWithCookie(loginRequest, response);

                // then
                cookieUtilMock.verify(() -> CookieUtil.createAccessTokenCookie("access.token.value", true), times(1));
                cookieUtilMock.verify(() -> CookieUtil.createRefreshTokenCookie("refresh.token.value", true), times(1));
            }
        }

        @Test
        @DisplayName("기타 환경 - secure=false 쿠키 설정 (기본값)")
        void otherEnvironment_SecureFalse() {
            // given
            ReflectionTestUtils.setField(authenticationService, "activeProfile", "test");
            given(memberService.login(loginRequest)).willReturn(loginResponse);
            given(memberService.getMemberById(1L)).willReturn(memberResponse);

            try (MockedStatic<CookieUtil> cookieUtilMock = Mockito.mockStatic(CookieUtil.class)) {
                Cookie accessTokenCookie = new Cookie("accessToken", "access.token.value");
                Cookie refreshTokenCookie = new Cookie("refreshToken", "refresh.token.value");

                cookieUtilMock.when(() -> CookieUtil.createAccessTokenCookie("access.token.value", false))
                    .thenReturn(accessTokenCookie);
                cookieUtilMock.when(() -> CookieUtil.createRefreshTokenCookie("refresh.token.value", false))
                    .thenReturn(refreshTokenCookie);

                // when
                authenticationService.loginWithCookie(loginRequest, response);

                // then
                cookieUtilMock.verify(() -> CookieUtil.createAccessTokenCookie("access.token.value", false), times(1));
                cookieUtilMock.verify(() -> CookieUtil.createRefreshTokenCookie("refresh.token.value", false), times(1));
            }
        }
    }

}
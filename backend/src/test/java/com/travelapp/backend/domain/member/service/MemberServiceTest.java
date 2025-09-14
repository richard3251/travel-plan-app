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
import com.travelapp.backend.domain.member.dto.request.MemberSignUpRequest;
import com.travelapp.backend.domain.member.dto.response.MemberLoginResponse;
import com.travelapp.backend.domain.member.dto.response.MemberResponse;
import com.travelapp.backend.domain.member.dto.response.TokenRefreshResponse;
import com.travelapp.backend.domain.member.entity.Member;
import com.travelapp.backend.domain.member.entity.Role;
import com.travelapp.backend.domain.member.exception.DuplicateEmailException;
import com.travelapp.backend.domain.member.exception.MemberNotFoundException;
import com.travelapp.backend.domain.member.repository.MemberRepository;
import com.travelapp.backend.global.exception.InvalidValueException;
import com.travelapp.backend.global.util.JwtUtil;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService 테스트")
public class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private MemberService memberService;

    private Member testMember;
    private MemberSignUpRequest signUpRequest;
    private MemberLoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
            .id(1L)
            .email("test@example.com")
            .nickname("테스트유저")
            .password("encodedPassword")
            .role(Role.USER)
            .build();

        signUpRequest = MemberSignUpRequest.builder()
            .email("newuser@example.com")
            .nickname("새로운유저")
            .password("password123!")
            .build();

        loginRequest = MemberLoginRequest.builder()
            .email("test@example.com")
            .password("password123!")
            .build();
    }

    @Nested
    @DisplayName("회원가입")
    class SignUp {

        @Test
        @DisplayName("성공 - 유효한 요청으로 회원가입을 완료한다")
        void signUp_Success() {
            // given
            given(memberRepository.existsByEmail(signUpRequest.getEmail())).willReturn(false);
            given(passwordEncoder.encode(signUpRequest.getPassword())).willReturn("encodePassword");
            given(memberRepository.save(any(Member.class))).willReturn(testMember);

            // when
            MemberResponse result = memberService.signUp(signUpRequest);

            // then
            assertThat(result.getEmail()).isEqualTo(testMember.getEmail());
            assertThat(result.getNickname()).isEqualTo(testMember.getNickname());
            verify(memberRepository).existsByEmail(signUpRequest.getEmail());
            verify(passwordEncoder).encode(signUpRequest.getPassword());
            verify(memberRepository).save(any(Member.class));
        }

        @Test
        @DisplayName("실패 - 이미 존재하는 이메일로 회원가입 시 예외 발생")
        void signUp_Fail_DuplicateEmail() {
            // given
            given(memberRepository.existsByEmail(signUpRequest.getEmail())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> memberService.signUp(signUpRequest))
                .isInstanceOf(DuplicateEmailException.class);

            verify(memberRepository).existsByEmail(signUpRequest.getEmail());
            verify(memberRepository, times(0)).save(any(Member.class));
        }
    }

    @Nested
    @DisplayName("로그인")
    class login {

        @Test
        @DisplayName("성공 - 유효한 자격증명으로 로그인한다")
        void login_Success() {
            // given
            String accessToken = "access.token.jwt";
            String refreshToken = "refresh.token.jwt";

            given(memberRepository.findByEmail(loginRequest.getEmail())).willReturn(Optional.of(testMember));
            given(passwordEncoder.matches(loginRequest.getPassword(), testMember.getPassword())).willReturn(true);
            given(jwtUtil.generateToken(testMember.getId(), testMember.getEmail())).willReturn(accessToken);
            given(jwtUtil.generateRefreshToken(testMember.getId())).willReturn(refreshToken);
            doNothing().when(refreshTokenService).saveRefreshToken(refreshToken, testMember.getId());

            // when
            MemberLoginResponse result = memberService.login(loginRequest);

            // then
            assertThat(result.getEmail()).isEqualTo(testMember.getEmail());
            assertThat(result.getAccessToken()).isEqualTo(accessToken);
            assertThat(result.getRefreshToken()).isEqualTo(refreshToken);

            verify(memberRepository).findByEmail(loginRequest.getEmail());
            verify(passwordEncoder).matches(loginRequest.getPassword(), testMember.getPassword());
            verify(jwtUtil).generateToken(testMember.getId(), testMember.getEmail());
            verify(jwtUtil).generateRefreshToken(testMember.getId());
            verify(refreshTokenService).saveRefreshToken(refreshToken, testMember.getId());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 이메일로 로그인 시 예외 발생")
        void login_Fail_MemberNotFound() {
            // given
            given(memberRepository.findByEmail(loginRequest.getEmail())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.login(loginRequest))
                .isInstanceOf(MemberNotFoundException.class);

            verify(memberRepository).findByEmail(loginRequest.getEmail());
            verify(passwordEncoder, times(0)).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("실패 - 잘못된 비밀번호로 로그인 시 예외 발생")
        void login_Fail_InvalidPassword() {
            // given
            given(memberRepository.findByEmail(loginRequest.getEmail())).willReturn(Optional.of(testMember));
            given(passwordEncoder.matches(loginRequest.getPassword(), testMember.getPassword())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> memberService.login(loginRequest))
                .isInstanceOf(InvalidValueException.class);

            verify(memberRepository).findByEmail(loginRequest.getEmail());
            verify(passwordEncoder).matches(loginRequest.getPassword(), testMember.getPassword());
        }
    }

    @Nested
    @DisplayName("회원 정보 조회")
    class GetMemberById {

        @Test
        @DisplayName("성공 - ID로 회원 정보를 조회한다")
        void getMemberById_Success() {

            // given
            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));

            // when
            MemberResponse result = memberService.getMemberById(1L);

            // then
            assertThat(result.getEmail()).isEqualTo(testMember.getEmail());
            assertThat(result.getNickname()).isEqualTo(testMember.getNickname());
            verify(memberRepository).findById(1L);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 ID로 조회 시 예외 발생")
        void getMemberById_Fail_MemberNotFound() {
            // given
            given(memberRepository.findById(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.getMemberById(1L))
                .isInstanceOf(MemberNotFoundException.class);

            verify(memberRepository).findById(1L);
        }
    }

    @Nested
    @DisplayName("토큰 갱신")
    class RefreshToken {

        @Test
        @DisplayName("성공 - 유효한 Refresh Token으로 새 토큰을 발급한다")
        void refreshToken_Success() {
            // given
            String oldRefreshToken = "old.refresh.token";
            String newAccessToken = "new.access.token";
            String newRefreshToken = "new.refresh.token";

            given(jwtUtil.validateToken(oldRefreshToken)).willReturn(true);
            given(jwtUtil.isRefreshToken(oldRefreshToken)).willReturn(true);
            given(refreshTokenService.existsRefreshToken(oldRefreshToken)).willReturn(true);
            given(refreshTokenService.getMemberIdByToken(oldRefreshToken)).willReturn(1L);
            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
            given(jwtUtil.generateToken(testMember.getId(), testMember.getEmail())).willReturn(newAccessToken);
            given(jwtUtil.generateRefreshToken(testMember.getId())).willReturn(newRefreshToken);
            doNothing().when(refreshTokenService).deleteRefreshToken(oldRefreshToken);
            doNothing().when(refreshTokenService).saveRefreshToken(newRefreshToken, testMember.getId());

            // when
            TokenRefreshResponse result = memberService.refreshToken(oldRefreshToken);

            // then
            assertThat(result.getAccessToken()).isEqualTo(newAccessToken);
            assertThat(result.getRefreshToken()).isEqualTo(newRefreshToken);

            verify(jwtUtil).validateToken(oldRefreshToken);
            verify(jwtUtil).isRefreshToken(oldRefreshToken);
            verify(refreshTokenService).existsRefreshToken(oldRefreshToken);
            verify(refreshTokenService).getMemberIdByToken(oldRefreshToken);
            verify(memberRepository).findById(1L);
            verify(refreshTokenService).deleteRefreshToken(oldRefreshToken);
            verify(refreshTokenService).saveRefreshToken(newRefreshToken, testMember.getId());
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 토큰으로 갱신 시 예외 발생")
        void refreshToken_Fail_InvalidToken() {
            // given
            String invalidToken = "invalid.token";
            given(jwtUtil.validateToken(invalidToken)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> memberService.refreshToken(invalidToken))
                .isInstanceOf(InvalidValueException.class);

            verify(jwtUtil).validateToken(invalidToken);
        }

        @Test
        @DisplayName("실패 - Access Token으로 갱신 시 예외 발생")
        void refreshToken_Fail_NotRefreshToken() {
            // given
            String accessToken = "access.token";
            given(jwtUtil.validateToken(accessToken)).willReturn(true);
            given(jwtUtil.isRefreshToken(accessToken)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> memberService.refreshToken(accessToken))
                .isInstanceOf(InvalidValueException.class);

            verify(jwtUtil).validateToken(accessToken);
            verify(jwtUtil).isRefreshToken(accessToken);
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class Logout {

        @Test
        @DisplayName("성공 - 유효한 Refresh Token으로 로그아웃한다")
        void logout_Success() {
            // given
            String refreshToken = "valid.refresh.token";
            given(refreshTokenService.existsRefreshToken(refreshToken)).willReturn(true);
            doNothing().when(refreshTokenService).deleteRefreshToken(refreshToken);

            // when
            memberService.logout(refreshToken);

            // then
            verify(refreshTokenService).existsRefreshToken(refreshToken);
            verify(refreshTokenService).deleteRefreshToken(refreshToken);
        }

        @Test
        @DisplayName("성공 - null 토큰으로 로그아웃 시 정상 처리")
        void logout_Success_NullToken() {
            // when
            memberService.logout(null);

            // then
            verify(refreshTokenService, times(0)).existsRefreshToken(anyString());
            verify(refreshTokenService, times(0)).deleteRefreshToken(anyString());
        }
    }

    @Nested
    @DisplayName("모든 기기에서 로그아웃")
    class LogoutFromAllDevices {

        @Test
        @DisplayName("성공 - 사용자의 모든 Refresh Token을 삭제한다")
        void logoutFromAllDevices_Success() {
            // given
            doNothing().when(refreshTokenService).deleteAllRefreshTokensByMemberId(1L);

            // when
            memberService.logoutFromAllDevices(1L);

            // then
            verify(refreshTokenService).deleteAllRefreshTokensByMemberId(1L);
        }

    }


}

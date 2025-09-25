package com.travelapp.backend.domain.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.travelapp.backend.domain.member.dto.request.MemberLoginRequest;
import com.travelapp.backend.domain.member.dto.request.MemberSignUpRequest;
import com.travelapp.backend.domain.member.dto.response.MemberResponse;
import com.travelapp.backend.domain.member.entity.Role;
import com.travelapp.backend.domain.member.service.AuthenticationService;
import com.travelapp.backend.domain.member.service.MemberService;
import com.travelapp.backend.global.exception.GlobalExceptionHandler;
import com.travelapp.backend.global.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberController 테스트")
public class MemberControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MemberService memberService;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private MemberController memberController;

    private ObjectMapper objectMapper;
    private MockedStatic<SecurityUtil> securityUtilMock;

    private MemberSignUpRequest signUpRequest;
    private MemberLoginRequest loginRequest;
    private MemberResponse memberResponse;

    @BeforeEach
    void setUp() {
        securityUtilMock = Mockito.mockStatic(SecurityUtil.class);
        securityUtilMock.when(SecurityUtil::getCurrentMemberId).thenReturn(1L);

        mockMvc = MockMvcBuilders.standaloneSetup(memberController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        signUpRequest = MemberSignUpRequest.builder()
            .email("test@example.com")
            .nickname("testUser")
            .password("password123!")
            .build();

        loginRequest = MemberLoginRequest.builder()
            .email("test@example.com")
            .password("password123!")
            .build();

        memberResponse = MemberResponse.builder()
            .id(1L)
            .email("test@example.com")
            .nickname("testUser")
            .role(Role.USER)
            .build();
    }

    @AfterEach
    void tearDown() {
        if (securityUtilMock != null) {
            securityUtilMock.close();
        }
    }

    @Nested
    @DisplayName("회원가입")
    class SignUp {

        @Test
        @DisplayName("성공 - 유효한 회원가입 요청")
        void signup_Success() throws Exception {
            // given
            given(memberService.signUp(any(MemberSignUpRequest.class))).willReturn(memberResponse);

            // when & then
            mockMvc.perform(post("/api/members/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("testUser"))
                .andExpect(jsonPath("$.role").value("USER"));
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 이메일 형식")
        void signUp_Fail_InvalidEmail() throws Exception {
            // given
            MemberSignUpRequest invalidRequest = MemberSignUpRequest.builder()
                .email("invalid-email")
                .nickname("testUser")
                .password("password123!")
                .build();

            // when & then
            mockMvc.perform(post("/api/members/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - 닉네임이 너무 짧음")
        void signup_Fail_ShortNickname() throws Exception {
            // given
            MemberSignUpRequest invalidRequest = MemberSignUpRequest.builder()
                .email("test@example.com")
                .nickname("a")
                .password("password123!")
                .build();

            // when & then
            mockMvc.perform(post("/api/members/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - 비밀번호 형식 불일치")
        void signup_Fail_InvalidPassword() throws Exception {
            // given
            MemberSignUpRequest invalidRequest = MemberSignUpRequest.builder()
                .email("test@example.com")
                .nickname("testUser")
                .password("weakpassword")
                .build();

            // when & then
            mockMvc.perform(post("/api/members/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("로그인")
    class Login {

        @Test
        @DisplayName("성공 - 유효한 로그인 요청")
        void login_Success() throws Exception {
            // given
            given(authenticationService.loginWithCookie(any(MemberLoginRequest.class),
                any(HttpServletResponse.class)))
                .willReturn(memberResponse);

            // when & then
            mockMvc.perform(post("/api/members/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("testUser"));
        }

        @Test
        @DisplayName("실패 - 이메일 누락")
        void login_Fail_MissingEmail() throws Exception {
            // given
            MemberLoginRequest invalidRequest = MemberLoginRequest.builder()
                .email("")
                .password("password123!")
                .build();

            // when & then
            mockMvc.perform(post("/api/members/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - 비밀번호 누락")
        void login_Fail_MissingPassword() throws Exception {
            // given
            MemberLoginRequest invalidRequest = MemberLoginRequest.builder()
                .email("test@example.com")
                .password("")
                .build();

            // when & then
            mockMvc.perform(post("/api/members/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("토큰 갱신")
    class RefreshToken {

        @Test
        @DisplayName("성공 - 토큰 갱신")
        void refreshToken_Success() throws Exception {
            // given
            doNothing().when(authenticationService)
                .refreshTokenWithCookie(any(HttpServletRequest.class),
                    any(HttpServletResponse.class));

            // when & then
            mockMvc.perform(post("/api/members/refresh"))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("현재 사용자 정보 조회")
    class GetCurrentMember {

        @Test
        @DisplayName("성공 - 인증된 사용자의 정보 조회")
        void getCurrentMember_Success() throws Exception {
            // given
            given(memberService.getMemberById(1L)).willReturn(memberResponse);

            // when & then
            mockMvc.perform(get("/api/members/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("testUser"))
                .andExpect(jsonPath("$.role").value("USER"));
        }

        @Test
        @DisplayName("실패 - 인증되지 않은 사용자")
        void getCurrentMember_Fail_Unauthorized() throws Exception {
            // given - SecurityUtil이 인증되지 않은 상태를 시뮬레이션
            securityUtilMock.when(SecurityUtil::getCurrentMemberId)
                .thenThrow(new IllegalStateException("인증되지 않은 사용자입니다."));

            // when & then
            mockMvc.perform(get("/api/members/me"))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class Logout {

        @Test
        @DisplayName("성공 - 로그아웃")
        void logout_Success() throws Exception {
            // given
            doNothing().when(authenticationService)
                .logoutWithCookie(any(HttpServletRequest.class), any(HttpServletResponse.class));

            // when & then
            mockMvc.perform(post("/api/members/logout"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("성공 - 모든 기기에서 로그아웃")
        void logoutFromAllDevices_Success() throws Exception {
            // given
            doNothing().when(memberService).logoutFromAllDevices(1L);

            // when & then
            mockMvc.perform(post("/api/members/logout-all"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("실패 - 인증되지 않은 사용자의 모든 기기 로그아웃 요청")
        void logoutFromAllDevices_Fail_Unauthorized() throws Exception {
            // given - SecurityUtil 이 인증되지 않은 상태를 시뮬레이션
            securityUtilMock.when(SecurityUtil::getCurrentMemberId)
                .thenThrow(new IllegalStateException("인증되지 않은 사용자입니다."));

            // when & then
            mockMvc.perform(post("/api/members/logout-all"))
                .andExpect(status().isUnauthorized());
        }

    }


}

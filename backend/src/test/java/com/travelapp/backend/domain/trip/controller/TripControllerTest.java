package com.travelapp.backend.domain.trip.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.travelapp.backend.domain.trip.dto.request.TripCreateRequest;
import com.travelapp.backend.domain.trip.dto.request.TripModifyRequest;
import com.travelapp.backend.domain.trip.dto.response.TripResponse;
import com.travelapp.backend.domain.trip.exception.TripAccessDeniedException;
import com.travelapp.backend.domain.trip.exception.TripNotFoundException;
import com.travelapp.backend.domain.trip.service.TripService;
import com.travelapp.backend.global.exception.GlobalExceptionHandler;
import com.travelapp.backend.global.util.SecurityUtil;
import java.time.LocalDate;
import java.util.List;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@DisplayName("TripController 테스트")
class TripControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TripService tripService;

    @InjectMocks
    private TripController tripController;

    @Autowired
    private ObjectMapper objectMapper;

    private MockedStatic<SecurityUtil> securityUtilMock;

    private TripCreateRequest createRequest;
    private TripModifyRequest modifyRequest;
    private TripResponse tripResponse;

    @BeforeEach
    void setUp() {
        securityUtilMock = Mockito.mockStatic(SecurityUtil.class);
        securityUtilMock.when(SecurityUtil::getCurrentMemberId).thenReturn(1L);

        mockMvc = MockMvcBuilders.standaloneSetup(tripController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        createRequest = TripCreateRequest.builder()
            .title("제주도 여행")
            .startDate(LocalDate.now().plusDays(1))
            .endDate(LocalDate.now().plusDays(4))
            .region("제주도")
            .latitude(33.4996)
            .longitude(126.5312)
            .build();

        modifyRequest = TripModifyRequest.builder()
            .title("수정된 제주도 여행")
            .startDate(LocalDate.of(2025, 6, 26))
            .endDate(LocalDate.of(2025, 6, 29))
            .region("제주도")
            .regionLat(33.4996)
            .regionLng(126.5312)
            .build();

        tripResponse = TripResponse.builder()
            .id(1L)
            .title("제주도 여행")
            .startDate(LocalDate.now().plusDays(1))
            .endDate(LocalDate.now().plusDays(4))
            .region("제주도")
            .regionLat(33.4996)
            .regionLng(126.5312)
            .build();
    }

    @AfterEach
    void tearDown() {
        if (securityUtilMock != null) {
            securityUtilMock.close();
        }
    }

    @Nested
    @DisplayName("여행 생성")
    class CreateTrip {

        @Test
        @DisplayName("성공 - 유효한 요청으로 여행을 생성한다")
        void createTrip_Success() throws Exception {

            // given
            doNothing().when(tripService).createTrip(anyLong(), any(TripCreateRequest.class));

            // when & then
            mockMvc.perform(post("/api/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 요청 데이터로 여행 생성 시 400 에러")
        void createTrip_Fail_InvalidRequest() throws Exception {
            // given
            TripCreateRequest invalidRequest = TripCreateRequest.builder()
                .title("")
                .startDate(LocalDate.of(2024, 12, 25))
                .endDate(LocalDate.of(2024, 12, 20))
                .region("제주도")
                .latitude(33.4996)
                .longitude(126.5312)
                .build();

            // when & then
            mockMvc.perform(post("/api/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - 인증되지 않은 사용자의 요청 시 401 에러")
        void createTrip_Fail_Unauthorized() throws Exception {
            // given
            securityUtilMock.when(SecurityUtil::getCurrentMemberId)
                    .thenThrow(new IllegalStateException("인증되지 않은 사용자입니다."));

            // when & then
            mockMvc.perform(post("/api/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("여행 목록 조회")
    class GetTrips {

        @Test
        @DisplayName("성공 - 사용자의 여행 목록을 조회한다")
        void getTrips_Success() throws Exception {
                // given
                List<TripResponse> trips = List.of(tripResponse);
                given(tripService.getTrips(1L)).willReturn(trips);

                // when & then
                mockMvc.perform(get("/api/trips"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].title").value("제주도 여행"))
                    .andExpect(jsonPath("$[0].region").value("제주도"));
        }

        @Test
        @DisplayName("성공 - 여행이 없는 경우 빈 배열을 반환한다")
        void getTrips_Success_EmptyList() throws Exception {
                // given
                given(tripService.getTrips(1L)).willReturn(List.of());

                // when & then
                mockMvc.perform(get("/api/trips"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("여행 상세 조회")
    class GetTrip {

        @Test
        @DisplayName("성공 - 여행 상세 정보를 조회한다")
        void getTrip_Success() throws Exception {
            // given
            given(tripService.getTrip(1L)).willReturn(tripResponse);

            // when & then
            mockMvc.perform(get("/api/trips/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("제주도 여행"))
                .andExpect(jsonPath("$.region").value("제주도"))
                .andExpect(jsonPath("$.regionLat").value(33.4996))
                .andExpect(jsonPath("$.regionLng").value(126.5312));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 여행 조회 시 404 에러")
        void getTrip_Fail_NotFound() throws Exception {
            // given
            given(tripService.getTrip(999L)).willThrow(new TripNotFoundException(999L));

            // when & then
            mockMvc.perform(get("/api/trips/999"))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("실패 - 접근 권한이 없는 여행 조회 시 403 에러")
        void getTrip_Fail_AccessDenied() throws Exception {
            // given
            given(tripService.getTrip(1L)).willThrow(new TripAccessDeniedException());

            // when & then
            mockMvc.perform(get("/api/trips/1"))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("여행 수정")
    class ModifyTrip {

        @Test
        @DisplayName("성공 - 여행 정보를 수정한다")
        void modifyTrip_Success() throws Exception {
            // given
            doNothing().when(tripService).modifyTrip(anyLong(), any(TripModifyRequest.class));

            // when & then
            mockMvc.perform(put("/api/trips/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(modifyRequest)))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 요청 데이터로 수정 시 400 에러")
        void modifyTrip_Fail_InvalidRequest() throws Exception {
            // given
            TripModifyRequest invalidRequest = TripModifyRequest.builder()
                .title("")
                .startDate(LocalDate.of(2024, 12, 25))
                .endDate(LocalDate.of(2024, 12, 20))
                .region("제주도")
                .regionLat(33.4996)
                .regionLng(126.5312)
                .build();

            // when & then
            mockMvc.perform(put("/api/trips/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - 접근 권한이 없는 여행 수정 시 403 에러")
        void modifyTrip_Fail_AccessDenied() throws Exception {
            // given
            doThrow(new TripAccessDeniedException()).when(tripService).modifyTrip(anyLong(), any(TripModifyRequest.class));

            // when & then
            mockMvc.perform(put("/api/trips/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(modifyRequest)))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("여행 삭제")
    class DeleteTrip {

        @Test
        @DisplayName("성공 - 여행을 삭제한다")
        void deleteTrip_Success() throws Exception {
            // given
            doNothing().when(tripService).deleteTrip(1L);

            // when & then
            mockMvc.perform(delete("/api/trips/1"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 여행 삭제 시 404 에러")
        void deleteTrip_Fail_NotFound() throws Exception {
            // given
            doThrow(new TripNotFoundException(999L)).when(tripService).deleteTrip(999L);

            // when & then
            mockMvc.perform(delete("/api/trips/999"))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("실패 - 접근 권한이 없는 여행 삭제 시 403 에러")
        void deleteTrip_Fail_AccessDenied() throws Exception {
            // given
            doThrow(new TripAccessDeniedException()).when(tripService).deleteTrip(1L);

            // when & then
            mockMvc.perform(delete("/api/trips/1"))
                .andExpect(status().isForbidden());
        }

    }
}

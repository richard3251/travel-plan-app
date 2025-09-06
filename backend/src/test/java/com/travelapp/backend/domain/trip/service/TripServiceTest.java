package com.travelapp.backend.domain.trip.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.travelapp.backend.domain.member.entity.Member;
import com.travelapp.backend.domain.member.exception.MemberNotFoundException;
import com.travelapp.backend.domain.member.repository.MemberRepository;
import com.travelapp.backend.domain.trip.dto.request.TripCreateRequest;
import com.travelapp.backend.domain.trip.dto.request.TripModifyRequest;
import com.travelapp.backend.domain.trip.dto.response.TripResponse;
import com.travelapp.backend.domain.trip.entity.Trip;
import com.travelapp.backend.domain.trip.exception.TripAccessDeniedException;
import com.travelapp.backend.domain.trip.exception.TripNotFoundException;
import com.travelapp.backend.domain.trip.repository.TripRepository;
import com.travelapp.backend.global.util.SecurityUtil;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("TripService 테스트")
public class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private TripService tripService;

    private Member testMember;
    private Trip testTrip;
    private TripCreateRequest createRequest;
    private TripModifyRequest modifyRequest;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
            .id(1L)
            .email("test@example.com")
            .nickname("테스트유저")
            .password("encodedPassword")
            .build();

        testTrip = Trip.builder()
            .id(1L)
            .member(testMember)
            .title("제주도 여행")
            .startDate(LocalDate.of(2024, 12, 25))
            .endDate(LocalDate.of(2024, 12, 28))
            .region("제주도")
            .regionLat(33.4996)
            .regionLng(126.5316)
            .build();

        createRequest = TripCreateRequest.builder()
            .title("부산 여행")
            .startDate(LocalDate.of(2024, 12, 30))
            .endDate(LocalDate.of(2024, 1, 2))
            .region("부산")
            .latitude(35.1595)
            .longitude(129.0756)
            .build();

        modifyRequest = TripModifyRequest.builder()
            .title("수정된 제주도 여행")
            .startDate(LocalDate.of(2024, 12, 26))
            .endDate(LocalDate.of(2024, 12, 29))
            .region("제주도")
            .regionLat(33.4996)
            .regionLng(126.5312)
            .build();
    }

    @Nested
    @DisplayName("여행 생성")
    class CreateTrip {

        @Test
        @DisplayName("성공 - 유효한 요청으로 여행을 생성한다")
        void createTrip_Success() {
            // given
            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
            given(tripRepository.save(any(Trip.class))).willReturn(testTrip);

            // when
            tripService.createTrip(1L, createRequest);

            // then
            verify(memberRepository).findById(1L);
            verify(tripRepository).save(any(Trip.class));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 회원으로 여행 생성 시 예외 발생")
        void createTrip_Fail_MemberNotFound() {
            // given
            given(memberRepository.findById(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> tripService.createTrip(1L, createRequest))
                .isInstanceOf(MemberNotFoundException.class);

            verify(memberRepository).findById(1L);
            verify(tripRepository, times(0)).save(any(Trip.class));
        }
    }

    @Nested
    @DisplayName("여행 목록 조회")
    class GetTrips {

        @Test
        @DisplayName("성공 - 회원의 모든 여행 목록을 조회한다")
        void getTrips_Success() {
            // given
            List<Trip> trips = List.of(testTrip);
            given(tripRepository.findByMember_Id(1L)).willReturn(trips);

            // when
            List<TripResponse> result = tripService.getTrips(1L);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("제주도 여행");
            verify(tripRepository).findByMember_Id(1L);
        }

        @Test
        @DisplayName("성공 - 여행이 없는 회원의 경우 빈 리스트를 반환한다")
        void getTrips_Success_EmptyList() {
            // given
            given(tripRepository.findByMember_Id(1L)).willReturn(List.of());

            // when
            List<TripResponse> result = tripService.getTrips(1L);

            // then
            assertThat(result).isEmpty();
            verify(tripRepository).findByMember_Id(1L);
        }
    }

    @Nested
    @DisplayName("여행 상세 조회")
    class GetTrip {

        @Test
        @DisplayName("성공 - 소유자가 여행을 조회한다")
        void getTrip_Success() {
            try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
                // given
                securityUtil.when(SecurityUtil :: getCurrentMemberId).thenReturn(1L);
                given(tripRepository.findById(1L)).willReturn(Optional.of(testTrip));

                // when
                TripResponse result = tripService.getTrip(1L);

                // then
                assertThat(result.getTitle()).isEqualTo("제주도 여행");
                assertThat(result.getRegion()).isEqualTo("제주도");
                verify(tripRepository).findById(1L);
            }
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 여행 조회 시 TripNotFoundException 예외 발생")
        void getTrip_Fail_TripNotFound() {
            // given
            given(tripRepository.findById(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> tripService.getTrip(1L))
                .isInstanceOf(TripNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - 소유자가 아닌 사용자가 여행 조회 시 예외 발생")
        void getTrip_Fail_AccessDenied() {
            try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
                // given
                securityUtil.when(SecurityUtil :: getCurrentMemberId).thenReturn(2L); // 다른 사용자
                given(tripRepository.findById(1L)).willReturn(Optional.of(testTrip));

                // when & then
                assertThatThrownBy(() -> tripService.getTrip(1L))
                    .isInstanceOf(TripAccessDeniedException.class);
            }
        }

        @Nested
        @DisplayName("여행 수정")
        class ModifyTrip {

            @Test
            @DisplayName("성공 - 소유자가 여행을 수정한다")
            void modifyTrip_Success() {
                try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
                    // given
                    securityUtil.when(SecurityUtil :: getCurrentMemberId).thenReturn(1L);
                    given(tripRepository.findById(1L)).willReturn(Optional.of(testTrip));
                    given(tripRepository.save(any(Trip.class))).willReturn(testTrip);

                    // when
                    tripService.modifyTrip(1L, modifyRequest);

                    // then
                    verify(tripRepository).findById(1L);
                    verify(tripRepository).save(any(Trip.class));
                }
            }

            @Test
            @DisplayName("실패 - 소유자가 아닌 사용자가 여행 수정 시 예외 발생")
            void modifyTrip_Fail_AccessDenied() {
                try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
                    // given
                    securityUtil.when(SecurityUtil::getCurrentMemberId).thenReturn(2L);
                    given(tripRepository.findById(1L)).willReturn(Optional.of(testTrip));

                    // when & then
                    assertThatThrownBy(() -> tripService.modifyTrip(1L, modifyRequest))
                        .isInstanceOf(TripAccessDeniedException.class);
                }
            }
        }

        @Nested
        @DisplayName("여행 삭제")
        class DeleteTrip {

            @Test
            @DisplayName("성공 - 소유자가 여행을 삭제한다")
            void deleteTrip_Success() {
                try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {

                    // given
                    securityUtil.when(SecurityUtil::getCurrentMemberId).thenReturn(1L);
                    given(tripRepository.findById(1L)).willReturn(Optional.of(testTrip));
                    doNothing().when(tripRepository).delete(testTrip);

                    // when
                    tripService.deleteTrip(1L);

                    // then
                    verify(tripRepository).findById(1L);
                    verify(tripRepository).delete(testTrip);
                }
            }

            @Test
            @DisplayName("실패 - 소유자가 아닌 사용자가 여행 삭제 시 예외 발생")
            void deleteTrip_Fail_AccessDenied() {
                try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
                    // given
                    securityUtil.when(SecurityUtil::getCurrentMemberId).thenReturn(2L);
                    given(tripRepository.findById(1L)).willReturn(Optional.of(testTrip));

                    // when & then
                    assertThatThrownBy(() -> tripService.deleteTrip(1L))
                        .isInstanceOf(TripAccessDeniedException.class);
                }
            }
        }

        @Nested
        @DisplayName("소유자 검증")
        class OwnerValidation {

            @Test
            @DisplayName("성공 - findTripWithOwnerValidation이 올바르게 작동한다")
            void findTripWithOwnerValidation_Success() {
                try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
                    // given
                    securityUtil.when(SecurityUtil::getCurrentMemberId).thenReturn(1L);
                    given(tripRepository.findById(1L)).willReturn(Optional.of(testTrip));

                    // when
                    Trip result = tripService.findTripWithOwnerValidation(1L);

                    // then
                    assertThat(result).isEqualTo(testTrip);
                    verify(tripRepository).findById(1L);
                }
            }


        }


    }


}

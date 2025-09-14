package com.travelapp.backend.domain.trip.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.travelapp.backend.domain.member.entity.Member;
import com.travelapp.backend.domain.member.entity.Role;
import com.travelapp.backend.domain.member.repository.MemberRepository;
import com.travelapp.backend.domain.trip.entity.Trip;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TripRepository 테스트")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan("com.travelapp.backend.domain")
@EnableJpaRepositories("com.travelapp.backend.domain")
public class TripRepositoryTest {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member testMember1;
    private Member testMember2;
    private Trip trip1;
    private Trip trip2;
    private Trip trip3;

    @BeforeEach
    void setUp() {
        // 테스트 회원 생성
        testMember1 = Member.builder()
            .email("user1@example.com")
            .nickname("사용자1")
            .password("encodePassword1")
            .role(Role.USER)
            .build();

        testMember2 = Member.builder()
            .email("user2@example.com")
            .nickname("사용자2")
            .password("encodedPassword2")
            .role(Role.USER)
            .build();

        memberRepository.save(testMember1);
        memberRepository.save(testMember2);

        // 테스트 여행 생성
        trip1 = Trip.builder()
            .member(testMember1)
            .title("제주도 여행")
            .startDate(LocalDate.of(2024, 12, 25))
            .endDate(LocalDate.of(2024, 12, 28))
            .region("제주도")
            .regionLat(33.4996)
            .regionLng(126.5312)
            .build();

        trip2 = Trip.builder()
            .member(testMember1)
            .title("부산 여행")
            .startDate(LocalDate.of(2025, 1, 1))
            .endDate(LocalDate.of(2025, 1, 3))
            .region("부산")
            .regionLat(35.1595)
            .regionLng(129.0756)
            .build();

        trip3 = Trip.builder()
            .member(testMember2)
            .title("서울 여행")
            .startDate(LocalDate.of(2025, 1, 10))
            .endDate(LocalDate.of(2025, 1, 12))
            .region("서울")
            .regionLat(37.5665)
            .regionLng(126.9780)
            .build();

        tripRepository.save(trip1);
        tripRepository.save(trip2);
        tripRepository.save(trip3);
    }

    @Test
    @DisplayName("특정 회원의 모든 여행을 조회한다")
    void findByMember_Id_Success() {
        // when
        List<Trip> trips = tripRepository.findByMember_Id(testMember1.getId());

        // then
        assertThat(trips).hasSize(2);
        assertThat(trips).extracting(Trip::getTitle)
            .containsExactlyInAnyOrder("제주도 여행", "부산 여행");
        assertThat(trips).allMatch(trip -> trip.getMember().getId().equals(testMember1.getId()));
    }

    @Test
    @DisplayName("여행이 없는 회원 조회 시 빈 리스트를 반환한다")
    void findByMember_Id_EmptyResult() {
        // given
        Member memberWithoutTrips = Member.builder()
            .email("notrip@example.com")
            .nickname("여행없는 사용자")
            .password("encodedPassword")
            .role(Role.USER)
            .build();
        memberRepository.save(memberWithoutTrips);

        // when
        List<Trip> trips = tripRepository.findByMember_Id(memberWithoutTrips.getId());

        // then
        assertThat(trips).isEmpty();
    }

    @Test
    @DisplayName("여행을 저장하고 조회할 수 있다")
    void saveAndFind_Success() {
        // given
        Trip newTrip = Trip.builder()
            .member(testMember1)
            .title("강릉 여행")
            .startDate(LocalDate.of(2025, 2, 1))
            .endDate(LocalDate.of(2025, 2, 3))
            .region("강릉")
            .regionLat(37.7519)
            .regionLng(128.8761)
            .build();

        // when
        Trip savedTrip = tripRepository.save(newTrip);

        // then
        assertThat(savedTrip.getId()).isNotNull();
        assertThat(savedTrip.getTitle()).isEqualTo("강릉 여행");
        assertThat(savedTrip.getMember().getId()).isEqualTo(testMember1.getId());

        // 저장된 여행 조회 확인
        Trip foundTrip = tripRepository.findById(savedTrip.getId()).orElse(null);
        assertThat(foundTrip).isNotNull();
        assertThat(foundTrip.getTitle()).isEqualTo("강릉 여행");
    }

    @Test
    @DisplayName("여행을 삭제할 수 있다")
    void delete_Success() {
        // given
        Long tripId = trip1.getId();

        // when
        tripRepository.delete(trip1);

        // then
        assertThat(tripRepository.findById(tripId)).isEmpty();

        // 다른 회원의 여행이나 같은 회원의 다른 여행은 영향받지 않음
        assertThat(tripRepository.findById(trip2.getId())).isPresent();
        assertThat(tripRepository.findById(trip3.getId())).isPresent();
    }

    @Test
    @DisplayName("여행 정보를 수정할 수 있다")
    void update_Success() {
        // given
        String newTitle = "수정된 제주도 여행";
        LocalDate newStartDate = LocalDate.of(2024, 12, 26);

        // when
        trip1.updateTitleAndStartDate(newTitle, newStartDate);

        Trip updatedTrip = tripRepository.save(trip1);

        // then
        assertThat(updatedTrip.getTitle()).isEqualTo(newTitle);
        assertThat(updatedTrip.getStartDate()).isEqualTo(newStartDate);
    }

}

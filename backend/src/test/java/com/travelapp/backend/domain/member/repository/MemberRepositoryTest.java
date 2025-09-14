package com.travelapp.backend.domain.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.travelapp.backend.domain.member.entity.Member;
import com.travelapp.backend.domain.member.entity.Role;
import java.util.Optional;
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
@DisplayName("MemberRepository 테스트")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan("com.travelapp.backend.domain.member.entity")
@EnableJpaRepositories("com.travelapp.backend.domain.member.repository")
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
            .email("test@example.com")
            .nickname("테스트유저")
            .password("encodedPassword")
            .role(Role.USER)
            .build();

        memberRepository.save(testMember);
    }

    @Test
    @DisplayName("이메일로 회원을 조회할 수 있다")
    void findByEmail_Success() {
        // when
        Optional<Member> foundMember = memberRepository.findByEmail("test@example.com");

        // then
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회 시 Empty를 반환한다")
    void findByEmail_NotFound() {
        // when
        Optional<Member> foundMember = memberRepository.findByEmail("notexist@example.com");

        // then
        assertThat(foundMember).isEmpty();
    }

    @Test
    @DisplayName("이메일 존재 여부를 확인할 수 있다")
    void existsByEmail_True() {
        // when
        boolean exists = memberRepository.existsByEmail("test@example.com");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 이메일은 false를 반환한다")
    void existsByEmail_False() {
        // when
        boolean exists = memberRepository.existsByEmail("notexist@example.com");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("회원을 저장하고 조회할 수 있다")
    void saveAndFind_Success() {
        // given
        Member newMember = Member.builder()
            .email("newuser@example.com")
            .nickname("새로운유저")
            .password("newEncodedPassword")
            .role(Role.USER)
            .build();

        // when
        Member savedMember = memberRepository.save(newMember);

        // then
        assertThat(savedMember.getId()).isNotNull();
        assertThat(savedMember.getEmail()).isEqualTo("newuser@example.com");
        assertThat(savedMember.getNickname()).isEqualTo("새로운유저");

        // 저장된 회원 조회 확인
        Optional<Member> foundMember = memberRepository.findById(savedMember.getId());
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getEmail()).isEqualTo("newuser@example.com");
    }

    @Test
    @DisplayName("회원을 삭제할 수 있다")
    void delete_Success() {
        // given
        Long memberId = testMember.getId();

        // when
        memberRepository.delete(testMember);

        // then
        Optional<Member> foundMember = memberRepository.findById(memberId);
        assertThat(foundMember).isEmpty();

        // 이메일로도 조회되지 않아야 함
        assertThat(memberRepository.findByEmail("test@example.com")).isEmpty();
        assertThat(memberRepository.existsByEmail("test@example.com")).isFalse();
    }

    @Test
    @DisplayName("대소문자를 구분하여 이메일을 조회한다")
    void findByEmail_CaseSensitive() {
        // when
        Optional<Member> foundWithUppercase = memberRepository.findByEmail("TEST@EXAMPLE.COM");
        Optional<Member> foundWithMixedCase = memberRepository.findByEmail("Test@Example.Com");

        // then
        assertThat(foundWithUppercase).isEmpty();
        assertThat(foundWithMixedCase).isEmpty();

        // 정확한 케이스로는 조회됨
        Optional<Member> foundWithCorrectCase = memberRepository.findByEmail("test@example.com");
        assertThat(foundWithCorrectCase).isPresent();
    }

    @Test
    @DisplayName("여러 회원을 저장하고 각각 조회할 수 있다")
    void saveMultipleMembers_Success() {
        // given
        Member member2 = Member.builder()
            .email("user2@example.com")
            .nickname("사용자2")
            .password("password2")
            .role(Role.USER)
            .build();

        Member member3 = Member.builder()
            .email("user3@example.com")
            .nickname("사용자3")
            .password("password3")
            .role(Role.USER)
            .build();

        // when
        memberRepository.save(member2);
        memberRepository.save(member3);

        // then
        assertThat(memberRepository.existsByEmail("test@example.com")).isTrue();
        assertThat(memberRepository.existsByEmail("user2@example.com")).isTrue();
        assertThat(memberRepository.existsByEmail("user3@example.com")).isTrue();

        // 각 회원이 올바르게 저장되었는지 확인
        Optional<Member> found2 = memberRepository.findByEmail("user2@example.com");
        Optional<Member> found3 = memberRepository.findByEmail("user3@example.com");

        assertThat(found2).isPresent();
        assertThat(found2.get().getNickname()).isEqualTo("사용자2");
        assertThat(found3).isPresent();
        assertThat(found3.get().getNickname()).isEqualTo("사용자3");
    }

}

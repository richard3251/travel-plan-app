package com.travelapp.backend.domain.member.service;

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
import com.travelapp.backend.global.exception.dto.ErrorCode;
import com.travelapp.backend.global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public MemberResponse signUp(MemberSignUpRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        Member member = Member.builder()
            .email(request.getEmail())
            .nickname(request.getNickname())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(Role.USER)
            .build();

        return MemberResponse.of(memberRepository.save(member));
    }

    @Transactional
    public MemberLoginResponse login(MemberLoginRequest request) {

        Member member = memberRepository.findByEmail(request.getEmail()).orElseThrow(
            MemberNotFoundException::new
        );

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new InvalidValueException(ErrorCode.INVALID_PASSWORD);
        }

        String accessToken = jwtUtil.generateToken(member.getId(), member.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(member.getId());

        return MemberLoginResponse.of(member, accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public MemberResponse getMemberById(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(
            MemberNotFoundException :: new
        );

        return MemberResponse.of(member);
    }

    @Transactional
    public TokenRefreshResponse refreshToken(String refreshToken) {

        // 리프레시 토큰 유효성 검증
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new InvalidValueException(ErrorCode.INVALID_TOKEN);
        }

        // 리프레시 토큰인지 확인
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new InvalidValueException(ErrorCode.INVALID_TOKEN);
        }

        // 토큰에서 사용자 ID 추출
        Long memberId = jwtUtil.getMemberIdFromToken(refreshToken);

        // 사용자 존재 여부 확인
        Member member = memberRepository.findById(memberId).orElseThrow(
            MemberNotFoundException::new
        );

        // 새로운 토큰 생성
        String newAccessToken = jwtUtil.generateToken(member.getId(), member.getEmail());
        String newRefreshToken = jwtUtil.generateRefreshToken(member.getId());

        return TokenRefreshResponse.of(newAccessToken, newRefreshToken);
    }



}

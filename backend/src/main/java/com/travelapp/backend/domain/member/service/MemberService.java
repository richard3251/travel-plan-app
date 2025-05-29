package com.travelapp.backend.domain.member.service;

import com.travelapp.backend.domain.member.dto.request.MemberLoginRequest;
import com.travelapp.backend.domain.member.dto.request.MemberSignUpRequest;
import com.travelapp.backend.domain.member.dto.response.MemberResponse;
import com.travelapp.backend.domain.member.entity.Member;
import com.travelapp.backend.domain.member.entity.Role;
import com.travelapp.backend.domain.member.exception.DuplicateEmailException;
import com.travelapp.backend.domain.member.exception.MemberNotFoundException;
import com.travelapp.backend.domain.member.repository.MemberRepository;
import com.travelapp.backend.global.exception.InvalidValueException;
import com.travelapp.backend.global.exception.dto.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

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
    public MemberResponse login(MemberLoginRequest request) {

        Member member = memberRepository.findByEmail(request.getEmail()).orElseThrow(
            MemberNotFoundException:: new
        );

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new InvalidValueException(ErrorCode.INVALID_PASSWORD);
        }

        return MemberResponse.of(member);
    }



}

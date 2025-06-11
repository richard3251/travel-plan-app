package com.travelapp.backend.domain.member.controller;

import com.travelapp.backend.domain.member.dto.request.MemberLoginRequest;
import com.travelapp.backend.domain.member.dto.request.MemberSignUpRequest;
import com.travelapp.backend.domain.member.dto.response.MemberLoginResponse;
import com.travelapp.backend.domain.member.dto.response.MemberResponse;
import com.travelapp.backend.domain.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<MemberResponse> signup(
       @Valid @RequestBody MemberSignUpRequest request
    ) {
        MemberResponse response = memberService.signUp(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<MemberLoginResponse> login(
        @Valid @RequestBody MemberLoginRequest request
    ) {
        MemberLoginResponse response = memberService.login(request);

        return ResponseEntity.ok(response);
    }

}

package com.travelapp.backend.global.controller;

import com.travelapp.backend.domain.member.dto.request.MemberSignUpRequest;
import com.travelapp.backend.domain.trip.dto.request.TripCreateRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/validation-test")
public class ValidationTestController {

    @PostMapping("/member-signup")
    public ResponseEntity<String> testMemberSignUpValidation(
        @Valid @RequestBody MemberSignUpRequest request
    ) {
        return ResponseEntity.ok("유효성 검증 통과!");
    }

    @PostMapping("/trip-create")
    public ResponseEntity<String> testTripCreateValidation(
        @Valid @RequestBody TripCreateRequest request
    ) {
        return ResponseEntity.ok("여행 생성 유효성 검증 통과!");
    }

}

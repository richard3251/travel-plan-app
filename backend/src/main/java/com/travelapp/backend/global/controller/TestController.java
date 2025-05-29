package com.travelapp.backend.global.controller;

import com.travelapp.backend.domain.member.exception.MemberNotFoundException;
import com.travelapp.backend.global.exception.InvalidValueException;
import com.travelapp.backend.global.exception.dto.ErrorCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/member-not-found")
    public String testMemberNotFoundException() {
        throw new MemberNotFoundException();
    }

    @GetMapping("/invalid-value")
    public String testInvalidValueException() {
        throw new InvalidValueException(ErrorCode.INVALID_INPUT_VALUE);
    }

    @GetMapping("/runtime-error")
    public String testRuntimeException() {
        throw new RuntimeException("테스트용 런타임 예외");
    }

    @GetMapping("/type-mismatch")
    public String testTypeMismatch(@RequestParam Integer number) {
        return "숫자: " + number;
    }
}
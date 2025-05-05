package com.travelapp.backend.domain.member.dto.request;

import lombok.Getter;

@Getter
public class MemberSignUpRequest {

    private String email;
    private String nickname;
    private String password;
}

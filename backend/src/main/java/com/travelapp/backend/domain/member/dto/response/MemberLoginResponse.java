package com.travelapp.backend.domain.member.dto.response;

import com.travelapp.backend.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberLoginResponse {

    private Long id;
    private String email;
    private String nickname;
    private String accessToken;

    public static MemberLoginResponse of(Member member, String accessToken) {
        return MemberLoginResponse.builder()
            .id(member.getId())
            .email(member.getEmail())
            .nickname(member.getNickname())
            .accessToken(accessToken)
            .build();
    }

}

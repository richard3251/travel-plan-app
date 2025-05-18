package com.travelapp.backend.domain.member.dto.response;

import com.travelapp.backend.domain.member.entity.Member;
import com.travelapp.backend.domain.member.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class MemberResponse {

    private Long id;
    private String email;
    private String nickname;
    private Role role;

    public static MemberResponse of(Member member) {
        return MemberResponse.builder()
            .id(member.getId())
            .email(member.getEmail())
            .nickname(member.getNickname())
            .role(member.getRole())
            .build();
    }

}

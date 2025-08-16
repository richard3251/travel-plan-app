package com.travelapp.backend.domain.member.dto.response;

import com.travelapp.backend.domain.member.entity.Member;
import com.travelapp.backend.domain.member.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "회원 정보 응답 DTO")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class MemberResponse {

    @Schema(description = "회원 ID", example = "1")
    private Long id;

    @Schema(description = "이메일 주소", example = "user@example.com")
    private String email;

    @Schema(description = "닉네임", example = "여행자123")
    private String nickname;

    @Schema(description = "사용자 권한", example = "USER")
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

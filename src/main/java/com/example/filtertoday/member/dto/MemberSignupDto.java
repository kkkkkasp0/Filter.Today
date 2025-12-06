package com.example.filtertoday.member.dto;

import com.example.filtertoday.common.Role;
import com.example.filtertoday.member.entity.Member;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MemberSignupDto {

    private String email;
    private String password;
    private String nickname;

    // DTO -> Entity 변환 편의 메서드 (비밀번호 암호화 후 전달받음)
    public Member toEntity(String encodedPassword) {
        return Member.builder()
                .email(this.email)
                .password(encodedPassword) // 암호화된 비밀번호
                .nickname(this.nickname)
                .role(Role.USER) // 가입 시 기본 권한은 USER
                .build();
    }
}
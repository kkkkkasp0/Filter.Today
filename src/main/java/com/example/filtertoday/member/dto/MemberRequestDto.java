package com.example.filtertoday.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class MemberRequestDto {
    private String email;
    private String password;
    private String nickname;
}

package com.example.filtertoday.member.controller;

import com.example.filtertoday.member.dto.MemberSignupDto;
import com.example.filtertoday.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 1. 로그인 페이지 보여주기
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // templates/login.html을 찾아감
    }

    // 2. 회원가입 페이지 보여주기
    @GetMapping("/signup")
    public String signupPage() {
        return "signup"; // templates/signup.html을 찾아감
    }

    // 3. 회원가입 실제 처리
    @PostMapping("/signup-process")
    public String signupProcess(MemberSignupDto memberDto) {
        System.out.println("회원가입 요청 데이터: " + memberDto.toString());
        memberService.join(memberDto); // 서비스에 회원가입 위임
        return "redirect:/login"; // 가입 성공하면 로그인 페이지로 이동
    }
}

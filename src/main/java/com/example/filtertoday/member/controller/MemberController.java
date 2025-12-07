package com.example.filtertoday.member.controller;

import com.example.filtertoday.member.dto.MemberRequestDto;
import com.example.filtertoday.member.dto.MemberSignupDto;
import com.example.filtertoday.member.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.PrintWriter;

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
    @PostMapping("/signup-process") // 혹은 "/signup-process" (본인 설정에 맞게)
    public String joinProcess(MemberSignupDto memberSignupDto, HttpServletResponse response) throws Exception {

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // 서비스에서 중복 체크 로직이 있다고 가정 (없으면 예외가 터짐)
            memberService.join(memberSignupDto);

            // 성공 시 메시지 띄우고 로그인 페이지로 이동
            out.println("<script>alert('회원가입이 완료되었습니다! 🎉 로그인해주세요.'); location.href='/login';</script>");
            out.flush();
            return null; // 뷰를 리턴하지 않고 직접 응답을 작성했으므로 null 리턴

        } catch (DataIntegrityViolationException e) {
            // 이메일 중복 등으로 DB 에러 발생 시
            out.println("<script>alert('이미 가입된 이메일입니다. 다른 이메일을 사용해주세요.'); history.back();</script>");
            out.flush();
            return null;
        } catch (Exception e) {
            // 기타 에러
            out.println("<script>alert('회원가입 중 오류가 발생했습니다.'); history.back();</script>");
            out.flush();
            return null;
        }
    }
}

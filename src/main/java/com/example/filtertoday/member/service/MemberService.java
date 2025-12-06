package com.example.filtertoday.member.service;

import com.example.filtertoday.common.Role;
import com.example.filtertoday.member.dto.MemberSignupDto;
import com.example.filtertoday.member.entity.Member;
import com.example.filtertoday.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Member getMember(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 없음"));
    }

    @Transactional // 쓰기 작업이므로 트랜잭션 필요
    public void join(MemberSignupDto memberSignupDto) {
        // 1. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(memberSignupDto.getPassword());

        // 2. DTO -> Entity 변환 (위에서 만든 toEntity 메서드 활용)
        Member member = memberSignupDto.toEntity(encodedPassword);

        // 3. DB 저장
        memberRepository.save(member);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1) DB에서 이메일로 회원 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));

        // 2) 찾은 회원 정보를 스프링 시큐리티가 이해할 수 있는 UserDetails 객체로 변환해서 반환
        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword()) // DB에 저장된 암호화된 비밀번호
                .roles(member.getRole().name()) // 예: "USER" -> 자동으로 "ROLE_USER"로 인식됨
                .build();
    }
}

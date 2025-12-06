package com.example.filtertoday.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 회원가입 시 비번 암호화에 필수
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 개발 편의상 일단 끔
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll() // 정적파일 허용
                        .requestMatchers("/", "/login", "/signup", "/signup-process").permitAll() // 누구나 접근 가능
                        .anyRequest().authenticated() // 나머지는 로그인해야 접근 가능
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login-process")
                        .usernameParameter("email") // ★ 이게 없으면 무조건 실패합니다!
                        .defaultSuccessUrl("/", true)      // ★ 로그인 성공하면 메인(/)으로 이동
                        .permitAll()
                );

        return http.build();
    }
}
package com.example.filtertoday.member.entity;

import com.example.filtertoday.common.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String email; // 로그인 ID

    @Column(nullable = false)
    private String password; // BCrypt로 해시된 비밀번호

    @Column(nullable = false, length = 20)
    private String nickname; // 사용자 닉네임

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default // 빌더 패턴 사용 시 기본값 설정
    private Role role = Role.USER; // 사용자 역할 (Enum)

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
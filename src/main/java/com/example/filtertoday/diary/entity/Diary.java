package com.example.filtertoday.diary.entity;

import com.example.filtertoday.common.EmotionType;
import com.example.filtertoday.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Diary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diary_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDate recordDate; // 기록 일자 (YYYY-MM-DD)

    @Column(nullable = false, length = 7)
    private String hexCode; // HEX 색상 코드 (예: #A3E4D7)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmotionType emotionType;

    @Column(columnDefinition = "TEXT") // 긴 텍스트 저장을 위해 TEXT 타입 사용
    private String content;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}

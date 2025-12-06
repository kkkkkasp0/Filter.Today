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
    private LocalDate recordDate;

    @Column(nullable = false, length = 7)
    private String hexCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmotionType emotionType;

    @Column(columnDefinition = "TEXT")
    private String content;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // ★ [추가] 일기 수정을 위한 비즈니스 메서드 (Dirty Checking 용)
    public void update(String content, String hexCode, EmotionType emotionType) {
        this.content = content;
        this.hexCode = hexCode;
        if (emotionType != null) {
            this.emotionType = emotionType;
        }
    }
}
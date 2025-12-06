package com.example.filtertoday.diary.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AiResponseDto {
    private String emotion;      // "JOY", "SADNESS" 등
    private double score;        // 정확도 점수
    private String korean_label; // "기쁨"
}
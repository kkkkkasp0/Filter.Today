package com.example.filtertoday.analysis.dto;

import com.example.filtertoday.common.EmotionType;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisToneMapResponseDto {
    //색상기록
    private LocalDate date;
    private String hexCode;
    private String content;
    private EmotionType emotionType;
}

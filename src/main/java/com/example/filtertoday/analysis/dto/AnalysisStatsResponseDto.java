package com.example.filtertoday.analysis.dto;

import com.example.filtertoday.common.EmotionType;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisStatsResponseDto {
    //통계차트
    private EmotionType emotionType;
    private String emotionType_label;
    private int count;
    private String hexCode;
}

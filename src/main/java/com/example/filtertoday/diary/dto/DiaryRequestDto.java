package com.example.filtertoday.diary.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class DiaryRequestDto {
    // JS에서 "2025-12-20" 처럼 문자열로 보내도 LocalDate로 자동 변환됨
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate recordDate;

    private String content;
    private String hexCode;
}
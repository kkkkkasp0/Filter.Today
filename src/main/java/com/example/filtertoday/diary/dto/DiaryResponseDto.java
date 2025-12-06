package com.example.filtertoday.diary.dto;

import com.example.filtertoday.diary.entity.Diary;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class DiaryResponseDto {
    private Long diaryId;
    private String content;
    private String hexCode;
    private String emotionType; // Enum을 문자열로 변환해서 전달

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate recordDate;

    // Entity -> DTO 변환 생성자
    public DiaryResponseDto(Diary diary) {
        this.diaryId = diary.getId();
        this.content = diary.getContent();
        this.hexCode = diary.getHexCode();
        this.emotionType = diary.getEmotionType().name(); // 예: "HAPPY"
        this.recordDate = diary.getRecordDate();
    }
}
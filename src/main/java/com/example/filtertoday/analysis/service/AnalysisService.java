package com.example.filtertoday.analysis.service;

import com.example.filtertoday.analysis.dto.AnalysisStatsResponseDto;
import com.example.filtertoday.analysis.dto.AnalysisToneMapResponseDto;
import com.example.filtertoday.analysis.repository.AnalysisRepository;
import com.example.filtertoday.common.EmotionType;
import com.example.filtertoday.diary.entity.Diary;
import com.example.filtertoday.diary.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final DiaryRepository diaryRepository;
    private final AnalysisRepository analysisRepository;

    // 톤 맵 데이터 조회 메서드
    public Map<String, AnalysisToneMapResponseDto> getToneMap(Long memberId, int year, int month) {
        List<Diary> diaries = getDiariesByMonth(memberId, year, month);

        Map<String, AnalysisToneMapResponseDto> responseMap = new HashMap<>();
        for (Diary diary : diaries) {
            responseMap.put(diary.getRecordDate().toString(), AnalysisToneMapResponseDto.builder()
                    .date(diary.getRecordDate())
                    .hexCode(diary.getHexCode())
                    .content(diary.getContent())
                    .emotionType(diary.getEmotionType())
                    .build());
        }
        return responseMap;
    }

    // 월별 일기 조회
    private List<Diary> getDiariesByMonth(Long memberId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        return diaryRepository.findByMemberIdAndRecordDateBetween(memberId, startDate, endDate);
    }
}

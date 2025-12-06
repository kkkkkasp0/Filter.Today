package com.example.filtertoday.analysis.service;

import com.example.filtertoday.analysis.dto.AnalysisStatsResponseDto;
import com.example.filtertoday.analysis.dto.AnalysisToneMapResponseDto;
import com.example.filtertoday.analysis.repository.AnalysisRepository;
import com.example.filtertoday.common.EmotionType;
import com.example.filtertoday.diary.entity.Diary;
import com.example.filtertoday.diary.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(readOnly = true)
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

    // 통계 데이터 조회 메서드
    @Transactional(readOnly = true)
    public List<AnalysisStatsResponseDto> getStats(Long memberId, int year, int month) {
        List<Diary> diaries = getDiariesByMonth(memberId, year, month); // 공통 메서드 재사용

        // 감성 타입별 개수 집계
        Map<EmotionType, Long> emotionCount = diaries.stream()
                .collect(Collectors.groupingBy(Diary::getEmotionType, Collectors.counting()));

        List<AnalysisStatsResponseDto> statsResponses = new ArrayList<>();

        for (Map.Entry<EmotionType, Long> entry : emotionCount.entrySet()) {
            EmotionType type = entry.getKey();
            Long count = entry.getValue();

            statsResponses.add(AnalysisStatsResponseDto.builder()
                    .emotionType(type)
                    .emotionType_label(type.name())
                    .count(count.intValue())
                    .hexCode(getRepresentativeColor(type)) // 색상 매핑
                    .build());
        }
        return statsResponses;
    }

    // (통계 전용) 대표 색상 반환
    private String getRepresentativeColor(EmotionType type) {
        return switch (type) {
            case JOY -> "#FFD700";
            case SADNESS -> "#4682B4";
            case ANGER -> "#FF4500";
            case ANXIETY -> "#800080";
            default -> "#808080";
        };
    }

    // 월별 일기 조회
    private List<Diary> getDiariesByMonth(Long memberId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        return diaryRepository.findByMemberIdAndRecordDateBetween(memberId, startDate, endDate);
    }
}

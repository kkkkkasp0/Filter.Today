package com.example.filtertoday.diary.service;

import com.example.filtertoday.common.EmotionType;
import com.example.filtertoday.diary.dto.AiResponseDto;
import com.example.filtertoday.diary.entity.Diary;
import com.example.filtertoday.diary.repository.DiaryRepository;
import com.example.filtertoday.diary.dto.DiaryRequestDto;
import com.example.filtertoday.diary.dto.DiaryResponseDto;
import com.example.filtertoday.member.entity.Member;
import com.example.filtertoday.member.repository.MemberRepository;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;
    private final Komoran komoran = new Komoran(DEFAULT_MODEL.FULL);
    private final WebClient webClient = WebClient.create("http://localhost:8000");

    // 1. 일기 조회 (날짜 기준)
    public DiaryResponseDto getDiaryByDate(String email, LocalDate date) {
        return diaryRepository.findByMemberEmailAndRecordDate(email, date)
                .map(DiaryResponseDto::new) // 있으면 DTO로 변환
                .orElse(null);              // 없으면 null 반환
    }

    // 2. 일기 저장
    @Transactional
    public void saveDiary(String email, DiaryRequestDto dto) {
        // 회원 찾기
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("회원을 찾을 수 없습니다."));

        // 해당 날짜에 이미 일기가 있는지 확인 (중복 방지 or 덮어쓰기 정책 결정)
        Optional<Diary> existingDiary = diaryRepository.findByMemberEmailAndRecordDate(email, dto.getRecordDate());

        if (existingDiary.isPresent()) {
            // 이미 있으면 내용만 수정 (덮어쓰기)
            updateDiaryLogic(existingDiary.get(), dto);
        } else {
            // 없으면 새로 생성
            Diary diary = Diary.builder()
                    .member(member)
                    .recordDate(dto.getRecordDate())
                    .content(dto.getContent())
                    .hexCode(dto.getHexCode())
                    .emotionType(determineEmotion(dto.getHexCode())) // 색상으로 감정 타입 결정
                    .build();
            diaryRepository.save(diary);
        }
    }

    // 3. 일기 수정 (ID 기준)
    @Transactional
    public void updateDiary(Long diaryId, DiaryRequestDto dto) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 일기가 존재하지 않습니다."));

        updateDiaryLogic(diary, dto);
    }

    // 4. 일기 삭제
    @Transactional
    public void deleteDiary(Long diaryId) {
        diaryRepository.deleteById(diaryId);
    }

    // [공통 로직] 수정 메서드
    private void updateDiaryLogic(Diary diary, DiaryRequestDto dto) {
        EmotionType type = determineEmotion(dto.getHexCode());
        diary.update(dto.getContent(), dto.getHexCode(), type);
    }

    // [헬퍼] 색상 코드로 감정 타입 결정하는 로직 (임시 구현)
    private EmotionType determineEmotion(String hexCode) {
        // TODO: 나중에 여기에 복잡한 AI 분석이나 색상 매핑 로직을 넣으세요.
        // 지금은 일단 모두 NORMAL로 하거나, 간단히 노란색 계열이면 HAPPY 등으로 분기 가능
        if (hexCode == null) return EmotionType.NORMAL;

        // 예시: 노란색(#FFFF00)이면 HAPPY
        if (hexCode.equalsIgnoreCase("#FFD700")) return EmotionType.JOY;
        // 예시: 파란색(#0000FF)이면 SAD
        if (hexCode.equalsIgnoreCase("#4682B4")) return EmotionType.SADNESS;

        return EmotionType.NORMAL; // 기본값
    }

    public EmotionType analyzeEmotion(String content) {
        if (content == null || content.trim().isEmpty()) return EmotionType.NORMAL;

        try {
            // 1. 파이썬 서버(/analyze)로 POST 요청 전송
            AiResponseDto response = webClient.post()
                    .uri("/analyze")
                    .bodyValue(Map.of("content", content)) // {"content": "일기내용"} JSON 전송
                    .retrieve()
                    .bodyToMono(AiResponseDto.class) // 응답을 DTO로 변환
                    .block(); // 결과가 올 때까지 기다림 (동기 처리)

            System.out.println("====== AI 응답 확인 ======");
            System.out.println("보낸 문장: " + content);
            System.out.println("AI가 준 감정(영어): " + response.getEmotion());
            System.out.println("AI가 준 라벨(한글): " + response.getKorean_label());
            System.out.println("========================");

            // 2. 결과가 오면 Enum으로 변환해서 반환
            if (response != null && response.getEmotion() != null) {
                System.out.println("AI 분석 결과: " + response.toString()); // 로그 확인용
                return EmotionType.valueOf(response.getEmotion());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Python 서버 연결 실패! 기본값으로 대체합니다.");
        }

        return EmotionType.NORMAL; // 실패 시 기본값
    }

    // 헬퍼 메서드: 리스트 안에 키워드가 포함되어 있는지 확인
    private boolean checkKeywords(List<String> extractedWords, String... keywords) {
        for (String word : extractedWords) {
            for (String key : keywords) {
                // 추출된 단어(word)가 키워드(key)를 포함하고 있는지 확인
                if (word.contains(key)) return true;
            }
        }
        return false;
    }

    // 헬퍼 메서드: 키워드가 하나라도 포함되어 있는지 확인
    private boolean containsKeyword(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }
}
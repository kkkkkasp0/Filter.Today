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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("회원을 찾을 수 없습니다."));

        Optional<Diary> existingDiary = diaryRepository.findByMemberEmailAndRecordDate(email, dto.getRecordDate());

        if (existingDiary.isPresent()) {
            updateDiaryLogic(existingDiary.get(), dto);
        } else {
            // ★ 핵심 로직: 사용자 선택 vs AI 분석 분기 처리

            // 1) 일단 사용자가 보낸 색깔이랑 가장 비슷한 감정을 수학적으로 찾아봅니다.
            EmotionType closestEmotion = findClosestEmotion(dto.getHexCode());

            String finalHex;
            EmotionType finalEmotion;

            // 2) 분기점: 사용자가 색을 직접 골랐나? (NORMAL이 아닌가?)
            if (closestEmotion != EmotionType.NORMAL) {
                // [CASE A] 직접 선택: 사용자가 고른 색을 유지하고, 감정만 그 색 계열로 맞춤
                finalHex = dto.getHexCode();
                finalEmotion = closestEmotion;
            } else {
                // [CASE B] 선택 안 함(기본값): AI가 글 내용을 읽고 감정과 색을 결정
                finalEmotion = analyzeEmotion(dto.getContent());
                finalHex = finalEmotion.getRepresentativeHexCode();
            }

            Diary diary = Diary.builder()
                    .member(member)
                    .recordDate(dto.getRecordDate())
                    .content(dto.getContent())
                    .hexCode(finalHex)
                    .emotionType(finalEmotion)
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

        // 1. 사용자가 보낸 색깔이 있는지(그리고 무슨 감정과 비슷한지) 확인
        EmotionType closestEmotion = findClosestEmotion(dto.getHexCode());

        String finalHex;
        EmotionType finalEmotion;

        // 2. 분기 처리 (저장 로직과 동일하게)
        if (closestEmotion != EmotionType.NORMAL) {
            // [CASE A] 직접 선택: 사용자가 고른 색 유지, 감정은 색 계열 따라감
            finalHex = dto.getHexCode();
            finalEmotion = closestEmotion;
        } else {
            // [CASE B] 선택 안 함: AI가 분석
            finalEmotion = analyzeEmotion(dto.getContent());
            finalHex = finalEmotion.getRepresentativeHexCode();
        }

        // 3. 최종 결과로 업데이트
        diary.update(dto.getContent(), finalHex, finalEmotion);
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

    private EmotionType findClosestEmotion(String inputHex) {
        if (inputHex == null || inputHex.isEmpty()) return EmotionType.NORMAL;

        // "NORMAL" 색상이거나 기본값이면 바로 NORMAL 리턴 (-> AI 분석으로 넘어감)
        if (inputHex.equalsIgnoreCase(EmotionType.NORMAL.getRepresentativeHexCode())
                || inputHex.equalsIgnoreCase("#E0E0E0")
                || inputHex.equalsIgnoreCase("#CCCCCC")) {
            return EmotionType.NORMAL;
        }

        EmotionType closestType = EmotionType.NORMAL;
        double minDistance = Double.MAX_VALUE;

        // RGB 변환
        int r1, g1, b1;
        try {
            r1 = Integer.valueOf(inputHex.substring(1, 3), 16);
            g1 = Integer.valueOf(inputHex.substring(3, 5), 16);
            b1 = Integer.valueOf(inputHex.substring(5, 7), 16);
        } catch (Exception e) {
            return EmotionType.NORMAL; // 색상 코드가 이상하면 NORMAL
        }

        // 모든 감정 Enum을 돌면서 거리 계산
        for (EmotionType type : EmotionType.values()) {
            if (type == EmotionType.NORMAL) continue; // NORMAL과는 비교 안 함

            String targetHex = type.getRepresentativeHexCode();
            int r2 = Integer.valueOf(targetHex.substring(1, 3), 16);
            int g2 = Integer.valueOf(targetHex.substring(3, 5), 16);
            int b2 = Integer.valueOf(targetHex.substring(5, 7), 16);

            // 거리 계산: sqrt((r1-r2)^2 + ...)
            double distance = Math.sqrt(Math.pow(r1 - r2, 2) + Math.pow(g1 - g2, 2) + Math.pow(b1 - b2, 2));

            if (distance < minDistance) {
                minDistance = distance;
                closestType = type;
            }
        }
        return closestType;
    }

    public List<Map<String, Object>> getTopKeywords(String email, int year, int month) {
        // 1. 해당 월의 일기 조회
        List<Diary> diaries = diaryRepository.findByMemberEmailAndRecordDateBetween(
                email,
                LocalDate.of(year, month, 1),
                LocalDate.of(year, month, 1).plusMonths(1).minusDays(1)
        );

        // 2. 일기 내용 합치기
        StringBuilder fullContent = new StringBuilder();
        for (Diary diary : diaries) {
            if (diary.getContent() != null) {
                fullContent.append(diary.getContent()).append(" ");
            }
        }

        if (fullContent.length() == 0) return List.of();

        try {
            // 3. 형태소 분석
            kr.co.shineware.nlp.komoran.model.KomoranResult analyzeResult = komoran.analyze(fullContent.toString());
            List<kr.co.shineware.nlp.komoran.model.Token> tokens = analyzeResult.getTokenList();

            Map<String, Integer> frequencyMap = new HashMap<>();

            for (kr.co.shineware.nlp.komoran.model.Token token : tokens) {
                String pos = token.getPos();
                String word = token.getMorph();

                // 명사(NN) 혹은 동사(VV)이면서, 2글자 이상인 것만 추출
                if ((pos.startsWith("NN") || pos.startsWith("VV")) && word.length() > 1) {
                    if (pos.startsWith("VV")) {
                        word = word + "다";
                    }
                    frequencyMap.put(word, frequencyMap.getOrDefault(word, 0) + 1);
                }
            }

            // 4. 많이 나온 순서대로 정렬 후 상위 10개 반환
            return frequencyMap.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .limit(10)
                    // ★ [수정된 부분] Map.<String, Object>of(...) 로 타입을 확실하게 고정
                    .map(entry -> Map.<String, Object>of(
                            "text", entry.getKey(),
                            "weight", entry.getValue()
                    ))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
    public String getNickname(String email) {
        return memberRepository.findByEmail(email)
                .map(member -> member.getNickname()) // Member 엔티티에 getNickname()이 있어야 함
                .orElse("사용자"); // 없으면 기본값
    }
}
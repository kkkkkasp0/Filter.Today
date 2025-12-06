package com.example.filtertoday.diary.service;

import com.example.filtertoday.common.EmotionType;
import com.example.filtertoday.diary.entity.Diary;
import com.example.filtertoday.diary.repository.DiaryRepository;
import com.example.filtertoday.diary.dto.DiaryRequestDto;
import com.example.filtertoday.diary.dto.DiaryResponseDto;
import com.example.filtertoday.member.entity.Member;
import com.example.filtertoday.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;

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
}
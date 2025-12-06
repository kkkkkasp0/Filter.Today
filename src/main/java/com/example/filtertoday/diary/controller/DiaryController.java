package com.example.filtertoday.diary.controller;

import com.example.filtertoday.diary.dto.DiaryRequestDto;
import com.example.filtertoday.diary.dto.DiaryResponseDto;
import com.example.filtertoday.diary.service.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    // 1. 일기 조회 (GET /api/diary?recordDate=2025-12-06)
    @GetMapping
    public ResponseEntity<DiaryResponseDto> getDiary(
            @RequestParam("recordDate") String dateStr,
            @AuthenticationPrincipal UserDetails userDetails // 현재 로그인한 사용자 정보
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build(); // 로그인 안 했으면 401
        }

        // 날짜 문자열(String)을 LocalDate로 변환
        LocalDate date = LocalDate.parse(dateStr);

        // 서비스에서 조회 (없으면 null 반환하거나 예외 처리)
        // 만약 데이터가 없으면 204(No Content)나 비어있는 JSON을 줘야 404 에러가 안 뜹니다.
        DiaryResponseDto diary = diaryService.getDiaryByDate(userDetails.getUsername(), date);

        if (diary == null) {
            return ResponseEntity.noContent().build(); // 데이터 없으면 204 반환 (에러 아님)
        }
        return ResponseEntity.ok(diary);
    }

    // 2. 일기 저장 (POST /api/diary)
    @PostMapping
    public ResponseEntity<String> saveDiary(
            @RequestBody DiaryRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) return ResponseEntity.status(401).build();

        diaryService.saveDiary(userDetails.getUsername(), requestDto);
        return ResponseEntity.ok("저장되었습니다.");
    }

    // 3. 일기 수정 (PUT /api/diary/{id})
    @PutMapping("/{id}")
    public ResponseEntity<String> updateDiary(
            @PathVariable Long id,
            @RequestBody DiaryRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        diaryService.updateDiary(id, requestDto);
        return ResponseEntity.ok("수정되었습니다.");
    }

    // 4. 일기 삭제 (DELETE /api/diary/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDiary(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        diaryService.deleteDiary(id);
        return ResponseEntity.ok("삭제되었습니다.");
    }
}

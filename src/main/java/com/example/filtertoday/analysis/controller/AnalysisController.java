package com.example.filtertoday.analysis.controller;

import com.example.filtertoday.analysis.dto.AnalysisStatsResponseDto;
import com.example.filtertoday.analysis.dto.AnalysisToneMapResponseDto;
import com.example.filtertoday.analysis.service.AnalysisService;
//import com.example.filtertoday.analysis.service.ColorClassificationService;
import com.example.filtertoday.member.entity.Member;
import com.example.filtertoday.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController // 1. JSON 응답을 위해 RestController 사용
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;
    private final MemberService memberService;

    @GetMapping("/tonemap")
    public ResponseEntity<Map<String, AnalysisToneMapResponseDto>> getToneMap(
            @RequestParam int year,
            @RequestParam int month,
            @AuthenticationPrincipal UserDetails userDetails) {
        Member member = getMember(userDetails); // 아래 공통 메서드 참고
        Map<String, AnalysisToneMapResponseDto> response = analysisService.getToneMap(member.getId(), year, month);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<List<AnalysisStatsResponseDto>> getStats(
            @RequestParam int year,
            @RequestParam int month,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Member member = getMember(userDetails);
        List<AnalysisStatsResponseDto> response = analysisService.getStats(member.getId(), year, month);
        return ResponseEntity.ok(response);
    }

    private Member getMember(UserDetails userDetails) {
        return memberService.getMember(userDetails.getUsername());
    }
}

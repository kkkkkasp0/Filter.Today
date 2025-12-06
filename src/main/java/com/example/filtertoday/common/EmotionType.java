package com.example.filtertoday.common;

import lombok.Getter;

@Getter
public enum EmotionType {
    JOY("기쁨", "#FFD700"), // 금색 계열
    CALM("차분함", "#ADD8E6"), // 하늘색 계열
    ANGER("분노", "#FF4500"), // 붉은 계열
    SADNESS("슬픔", "#4682B4"), // 푸른 계열
    ANXIETY("불안", "#808080"), // 회색 계열
    NORMAL("보통", "#E0E0E0");

    private final String koreanName;
    private final String representativeHexCode;

    EmotionType(String koreanName, String representativeHexCode) {
        this.koreanName = koreanName;
        this.representativeHexCode = representativeHexCode;
    }

    // Enum을 활용하는 메서드 (예시)
    // public static EmotionType findByHexRange(String hexCode) { ... }
}
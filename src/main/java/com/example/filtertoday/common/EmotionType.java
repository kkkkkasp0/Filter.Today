package com.example.filtertoday.common;

import lombok.Getter;

@Getter
public enum EmotionType {
    JOY("즐거움", "#FFD700"),
    EXCITEMENT("설렘", "#FFA500"),
    PROUD("뿌듯함", "#32CD32"),
    CALM("차분함", "#ADD8E6"),
    STRESS("스트레스", "#9370DB"),
    ANGER("분노", "#FF4500"),
    SADNESS("슬픔", "#4682B4"),
    TIRED("피곤함", "#A9A9A9"),
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
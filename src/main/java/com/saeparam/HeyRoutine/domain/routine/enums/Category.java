package com.saeparam.HeyRoutine.domain.routine.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 루틴 및 이모지의 카테고리
 * <p>
 *  API 요청 시 한글 이름 또는 영문 Enum 이름으로도 파싱될 수 있도록
 *  {@link #from(String)} 메서드를 제공합니다.
 * </p>
 */
@Getter
@RequiredArgsConstructor
public enum Category { // 카테고리
    LIFE("생활"),
    CONSUMPTION("소비"),
    EATING("식사"),
    STUDY("학습"),
    HEALTH("건강"),
    HOBBY("취미");

    private final String name;

    private static final Map<String, Category> LOOKUP = new HashMap<>();

    static {
        for (Category category : values()) {
            LOOKUP.put(category.name(), category);               // 영문 Enum 명칭
            LOOKUP.put(category.name.toUpperCase(), category);    // 한글 명칭
        }
    }

    @JsonCreator
    public static Category from(String value) {
        if (value == null) {
            return null;
        }
        return LOOKUP.get(value.toUpperCase());
    }
}

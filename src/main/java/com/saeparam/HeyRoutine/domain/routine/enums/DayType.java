package com.saeparam.HeyRoutine.domain.routine.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

/**
 * 요일 정보를 한글로 표현하면서 영문 축약형과 풀네임도 함께 보관합니다.
 * 클라이언트가 "MON" 혹은 "MONDAY"와 같이 영문으로 전달하더라도 {@link #from(String)}을
 * 통해 일관된 처리가 가능하도록 구성했습니다.
 */
@Getter
@RequiredArgsConstructor
public enum DayType {
    월("MON", "월요일"),
    화("TUE", "화요일"),
    수("WED", "수요일"),
    목("THU", "목요일"),
    금("FRI", "금요일"),
    토("SAT", "토요일"),
    일("SUN", "일요일");

    private final String shortName;
    private final String fullName;

    private static final Map<String, DayType> LOOKUP = new HashMap<>();

    static {
        for (DayType day : values()) {
            LOOKUP.put(day.name(), day); // 한글 요일
            LOOKUP.put(day.shortName.toUpperCase(), day); // 영문 축약
            LOOKUP.put(day.fullName.toUpperCase(), day); // 영문 풀네임
        }
    }

    /**
     * 문자열을 {@link DayType}으로 변환합니다.
     * 전달된 문자열이 존재하지 않는 요일일 경우 {@code null}을 반환합니다.
     */
    @JsonCreator
    public static DayType from(String value) {
        if (value == null) {
            return null;
        }
        return LOOKUP.get(value.toUpperCase());
    }

    public static DayType from(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY: return 월;
            case TUESDAY: return 화;
            case WEDNESDAY: return 수;
            case THURSDAY: return 목;
            case FRIDAY: return 금;
            case SATURDAY: return 토;
            case SUNDAY: return 일;
            default:
                throw new IllegalArgumentException("유효하지 않은 DayOfWeek 값입니다: " + dayOfWeek);
        }
    }
}
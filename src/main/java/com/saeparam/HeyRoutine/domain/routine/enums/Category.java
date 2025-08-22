package com.saeparam.HeyRoutine.domain.routine.enums;

import lombok.Getter;

@Getter
public enum Category { // 카테고리
    LIFE("생활"),
    CONSUMPTION("소비"),
    EATING("식사"),
    STUDY("학습"),
    HEALTH("건강"),
    HOBBY("취미");

    private final String name;

    Category(String name) {
        this.name = name;
    }
}

package com.saeparam.HeyRoutine.domain.routine.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * <h2>RoutineType</h2>
 * <p>
 * 단체/개인 루틴에서 사용하는 루틴의 종류를 정의합니다.
 * API 명세에서는 <code>DAILY</code>와 <code>FINANCE</code> 값을 사용하므로
 * 이를 Enum 이름으로 채택하고, 각 항목의 한글 설명을 별도로 보관합니다.
 * </p>
 */
@Getter
@RequiredArgsConstructor
public enum RoutineType {
    DAILY("생활"),
    FINANCE("소비");

    private final String description;
}
package com.saeparam.HeyRoutine.domain.analysis.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategorySpendingDto {
    private String categoryName; // 카테고리명 (예: 쇼핑, 식비)
    private long amount; // 해당 카테고리 지출 금액
    private double percentage; // 전체 지출에서 차지하는 비율
}
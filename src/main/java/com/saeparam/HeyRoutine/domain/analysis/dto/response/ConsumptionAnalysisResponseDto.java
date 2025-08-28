package com.saeparam.HeyRoutine.domain.analysis.dto.response;

import java.util.List;

import com.saeparam.HeyRoutine.domain.analysis.dto.CategorySpendingDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConsumptionAnalysisResponseDto {
    private long averageSpendingFor20s; // 20대 평균 지출
    private long myTotalSpending; // 나의 총 지출
    private int comparisonPercentage; // 평균 대비 지출 퍼센트
    private List<CategorySpendingDto> categorySpendings; // 카테고리별 지출 내역
}
package com.saeparam.HeyRoutine.domain.analysis.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주간 소비 분석 AI 응답 DTO
 */
@Getter
@NoArgsConstructor
public class WeeklySpendingAnalysisAiResponseDto {
    private List<String> analysis;
}
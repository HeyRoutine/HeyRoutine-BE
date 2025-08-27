package com.saeparam.HeyRoutine.domain.analysis.dto.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주간 소비 분석 AI 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WeeklySpendingAnalysisAiRequestDto {
    private List<GeminiReqDto> contents;
}
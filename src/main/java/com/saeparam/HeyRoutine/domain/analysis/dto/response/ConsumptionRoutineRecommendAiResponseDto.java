package com.saeparam.HeyRoutine.domain.analysis.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소비 루틴 맞춤 추천 AI 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumptionRoutineRecommendAiResponseDto {

	private ConsumptionRoutineRecommendResponseDto.ConsumerInfo analysis;
	private List<RoutineInfo> recommendRoutine;

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class RoutineInfo {
		private Long emojiId;
		private String routineName;
	}
}
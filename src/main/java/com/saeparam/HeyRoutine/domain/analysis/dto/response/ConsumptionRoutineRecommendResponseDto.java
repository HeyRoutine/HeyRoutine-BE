package com.saeparam.HeyRoutine.domain.analysis.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소비 루틴 맞춤 추천 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumptionRoutineRecommendResponseDto {

	private ConsumerInfo analysis;
	private List<RoutineInfo> recommendRoutine;

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class ConsumerInfo {
		private String consumerType;
		private String text;
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class RoutineInfo {
		private Long emojiId;
		private String routineName;
	}
}
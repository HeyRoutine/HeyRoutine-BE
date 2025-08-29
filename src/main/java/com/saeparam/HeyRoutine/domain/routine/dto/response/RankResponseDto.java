package com.saeparam.HeyRoutine.domain.routine.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class RankResponseDto {

	@Getter
	@Builder
	public static class RankInfo {
		private int rank;
		private String name;
		private int score;
	}

	@Getter
	@Builder
	public static class MyRankInfo {
		private int rank;
		private String universityName;
		private String majorName;
		private int score;
	}

	@Getter
	@Builder
	public static class RankPage {
		private int page;
		private int pageSize;
		private long totalItems;
		private int totalPages;
		private MyRankInfo myItem;
		private List<RankInfo> items;
	}
}
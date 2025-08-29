package com.saeparam.HeyRoutine.domain.user.dto.response;

import com.saeparam.HeyRoutine.domain.user.entity.University;
import com.saeparam.HeyRoutine.domain.user.entity.Major;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchInfoDto {
	private Long id;
	private String name;

	public static SearchInfoDto fromUniversity(University university) {
		return SearchInfoDto.builder()
			.id(university.getId())
			.name(university.getName())
			.build();
	}

	public static SearchInfoDto fromMajor(Major major) {
		return SearchInfoDto.builder()
			.id(major.getId())
			.name(major.getName())
			.build();
	}
}
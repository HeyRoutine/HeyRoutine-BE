package com.saeparam.HeyRoutine.domain.routine.dto.response;

import com.saeparam.HeyRoutine.domain.routine.entity.MyRoutineList;
import com.saeparam.HeyRoutine.domain.routine.enums.DayType;
import com.saeparam.HeyRoutine.domain.routine.enums.RoutineType;

import lombok.*;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 개인 루틴 목록 전체 조회 Response
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
public class MyRoutineListShowResponseDto {
	/** 개인 루틴 리스트 고유 ID */
	private Long id;

	/** 루틴 타입 (DAILY: 일상, FINANCE: 소비) */
	private RoutineType routineType;

	/** 개인 루틴 타이틀 */
	private String title;

	/** 루틴 시작 시간 (HH:mm) */
	private String startTime;

	/** 루틴 종료 시간 (HH:mm) */
	private String endTime;

	/** 루틴 개수 */
	private int routineNums;

	/** 현재 루틴 진행률(%) */
	private double percent;

	/** 루틴 수행 요일 리스트 */
	private List<String> dayOfWeek;

	/** 이번 주 성공한 요일 리스트 */
	private List<String> successDay;

	public static MyRoutineListShowResponseDto toDto(MyRoutineList myRoutineList) {
		List<String> days = myRoutineList.getRoutineDays().stream()
			.map(routineDay -> routineDay.getDayType().name())
			.collect(Collectors.toList());
        return MyRoutineListShowResponseDto.builder()
            .id(myRoutineList.getId())
            .title(myRoutineList.getTitle())
            .startTime(myRoutineList.getStartTime().toString())
            .endTime(myRoutineList.getEndTime().toString())
            .routineNums(0)
            .percent(0)
            .dayOfWeek(days)
            .successDay(Collections.emptyList())
            .build();
	}
}

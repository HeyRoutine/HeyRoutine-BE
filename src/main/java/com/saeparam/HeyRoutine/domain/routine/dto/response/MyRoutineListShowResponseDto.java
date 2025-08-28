package com.saeparam.HeyRoutine.domain.routine.dto.response;

import com.saeparam.HeyRoutine.domain.routine.entity.MyRoutineList;
import com.saeparam.HeyRoutine.domain.routine.enums.DayType;
import com.saeparam.HeyRoutine.domain.routine.enums.RoutineType;
import lombok.*;

import java.time.LocalTime;
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
    private Long id;
    private String title;
    private LocalTime startTime;
    private LocalTime endTime;
    private RoutineType routineType;
    private Set<DayType> dayTypes;
    /** 현재 루틴 진행률(%) */
    private double percent;
    /** 이번 주 성공한 요일 리스트 */
    private List<String> successDay;

    public static MyRoutineListShowResponseDto toDto(MyRoutineList myRoutineList){
        Set<DayType> days = myRoutineList.getRoutineDays().stream()
                .map(routineDay -> routineDay.getDayType())
                .collect(Collectors.toSet());
        return MyRoutineListShowResponseDto.builder()
                .id(myRoutineList.getId())
                .title(myRoutineList.getTitle())
                .startTime(myRoutineList.getStartTime())
                .endTime(myRoutineList.getEndTime())
                .routineType(myRoutineList.getRoutineType())
                .dayTypes(days)
                .percent(0)
                .build();
    }
}

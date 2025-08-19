package com.saeparam.HeyRoutine.domain.routine.dto.request;

import com.saeparam.HeyRoutine.domain.routine.entity.MyRoutineList;
import com.saeparam.HeyRoutine.domain.routine.enums.DayType;
import com.saeparam.HeyRoutine.domain.routine.enums.RoutineType;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyRoutineListMakeRequestDto {
    private String title;
    private LocalDateTime startDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private RoutineType routineType;
    private List<DayType> dayTypes;

    public static MyRoutineList toEntity(MyRoutineListMakeRequestDto myRoutineListMakeRequestDto, User user){
        return MyRoutineList.builder()
                .title(myRoutineListMakeRequestDto.getTitle())
                .startDate(myRoutineListMakeRequestDto.getStartDate())
                .startTime(myRoutineListMakeRequestDto.getStartTime())
                .endTime(myRoutineListMakeRequestDto.getEndTime())
                .routineType(myRoutineListMakeRequestDto.getRoutineType())
                .user(user)
                .build();
    }
}

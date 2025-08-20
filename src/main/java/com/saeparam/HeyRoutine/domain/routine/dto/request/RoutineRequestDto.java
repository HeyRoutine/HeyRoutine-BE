package com.saeparam.HeyRoutine.domain.routine.dto.request;

import com.saeparam.HeyRoutine.domain.routine.entity.Emoji;
import com.saeparam.HeyRoutine.domain.routine.entity.MyRoutineList;
import com.saeparam.HeyRoutine.domain.routine.entity.Routine;
import com.saeparam.HeyRoutine.domain.routine.enums.DayType;
import com.saeparam.HeyRoutine.domain.routine.enums.RoutineType;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;


/**
 * 루틴 만들기 Request
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoutineRequestDto {
    private String routineName;
    private Long emojiId;
    private int time;

    public static Routine toEntity(RoutineRequestDto routineRequestDto, Emoji emoji){
        return Routine.builder()
                .emoji(emoji)
                .name(routineRequestDto.getRoutineName())
                .time(routineRequestDto.getTime())
                .build();
    }

}

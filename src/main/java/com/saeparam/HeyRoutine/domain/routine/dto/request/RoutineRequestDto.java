package com.saeparam.HeyRoutine.domain.routine.dto.request;

import com.saeparam.HeyRoutine.domain.routine.entity.Emoji;
import com.saeparam.HeyRoutine.domain.routine.entity.Routine;
import lombok.*;


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

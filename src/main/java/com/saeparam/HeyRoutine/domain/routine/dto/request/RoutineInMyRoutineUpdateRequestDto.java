package com.saeparam.HeyRoutine.domain.routine.dto.request;

import com.saeparam.HeyRoutine.domain.routine.entity.Emoji;
import com.saeparam.HeyRoutine.domain.routine.entity.Routine;
import lombok.*;

import java.util.List;


/**
 * 루틴 만들기 Request
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoutineInMyRoutineUpdateRequestDto {

    private List<RoutineUpdateRequestDto> updateRoutine;
    private List<RoutineRequestDto> makeRoutine;



}

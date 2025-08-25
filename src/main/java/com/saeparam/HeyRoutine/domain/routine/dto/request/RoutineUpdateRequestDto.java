package com.saeparam.HeyRoutine.domain.routine.dto.request;

import com.saeparam.HeyRoutine.domain.routine.entity.Emoji;
import com.saeparam.HeyRoutine.domain.routine.entity.Routine;
import lombok.*;
import lombok.experimental.SuperBuilder;


/**
 * 루틴 업데이트 Request
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor

public class RoutineUpdateRequestDto extends RoutineRequestDto {
    private Long id;

}

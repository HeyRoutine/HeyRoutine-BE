package com.saeparam.HeyRoutine.domain.routine.dto.response;

import com.saeparam.HeyRoutine.domain.routine.entity.Emoji;
import com.saeparam.HeyRoutine.domain.routine.entity.Routine;
import lombok.*;


/**
 * 루틴 반환하기 Response
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoutineResponseDto {
    private String routineName;
    private String emojiUrl;
    private int time;

    public static RoutineResponseDto toDto(Routine routine){
        return RoutineResponseDto.builder()
                .emojiUrl(routine.getEmoji().getEmojiUrl())
                .time(routine.getTime())
                .routineName(routine.getName())
                .build();
    }

}

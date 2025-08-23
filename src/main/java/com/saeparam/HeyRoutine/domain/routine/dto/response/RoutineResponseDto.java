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
    private Long routineId;
    private String routineName;
    private String emojiUrl;
    private int time;
    private boolean isCompleted;

//    public static RoutineResponseDto toDto(Routine routine){
//        return RoutineResponseDto.builder()
//                .routineId(routine.getId())
//                .emojiUrl(routine.getEmoji().getEmojiUrl())
//                .time(routine.getTime())
//                .routineName(routine.getName())
//                .build();
//    }

    public static RoutineResponseDto toDto(Routine routine, boolean isCompleted) {
        return RoutineResponseDto.builder()
                .routineId(routine.getId())
                .routineName(routine.getName())
                .emojiUrl(routine.getEmoji().getEmojiUrl())
                .time(routine.getTime())
                .isCompleted(isCompleted)
                .build();
    }

}

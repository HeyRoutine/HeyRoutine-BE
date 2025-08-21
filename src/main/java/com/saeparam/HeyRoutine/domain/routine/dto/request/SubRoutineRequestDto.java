package com.saeparam.HeyRoutine.domain.routine.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * <h2>SubRoutineRequestDto</h2>
 * <p>
 * 단체 루틴에 속한 상세 루틴(Sub Routine) 관련 API의 <b>요청(Request)</b> 데이터 전송을 담당하는 DTO 클래스들을 포함합니다.<br>
 * 각 기능에 최적화된 요청 클래스를 내부 정적 클래스로 정의하여 응집도를 높였습니다.
 * </p>
 *
 * <pre>
 * ┌───────────────────────────┐
 * │ SubRoutineRequestDto      │
 * ├───────────────────────────┤
 * │ - Create                  │──► 상세 루틴 생성 요청
 * │ - Update                  │──► 상세 루틴 수정 요청
 * │ - StatusUpdate            │──► 상세 루틴 상태(성공/실패) 변경 요청
 * └───────────────────────────┘
 * </pre>
 */
public class SubRoutineRequestDto {

    /**
     * <h3>상세 루틴 생성 요청 DTO</h3>
     * <p>POST /api/v1/routines/groups/{groupRoutineListId}/sub-routines</p>
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @Schema(description = "상세 루틴 생성 요청 DTO")
    public static class Create {

        @Valid // routines 리스트 내부의 객체들도 유효성 검사를 수행합니다.
        @NotNull(message = "루틴 목록은 필수입니다.")
        @Size(min = 1, message = "루틴은 최소 1개 이상 등록해야 합니다.")
        @Schema(description = "생성할 상세 루틴 정보 리스트")
        private List<RoutineData> routines;

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @ToString
        @Schema(description = "생성할 개별 상세 루틴 정보")
        public static class RoutineData {

            @Schema(description = "연결된 템플릿 ID (선택)", example = "1")
            private Long templateId;

            @NotNull(message = "이모지 ID는 필수입니다.")
            @Schema(description = "선택한 이모지 ID", example = "1")
            private Long emojiId;

            @NotBlank(message = "루틴 이름은 필수입니다.")
            @Size(max = 50, message = "루틴 이름은 최대 50자까지 입력 가능합니다.")
            @Schema(description = "루틴 이름", example = "커피 내리기")
            private String name;

            @NotNull(message = "소요 시간은 필수입니다.")
            @Min(value = 1, message = "소요 시간은 1분 이상이어야 합니다.")
            @Max(value = 999, message = "소요 시간은 999분 이하여야 합니다.")
            @Schema(description = "루틴 소요 시간(분)", example = "10")
            private Integer time;
        }
    }

    /**
     * <h3>상세 루틴 수정 요청 DTO</h3>
     * <p>PUT /api/v1/routines/groups/{groupRoutineListId}/sub-routines</p>
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @Schema(description = "상세 루틴 수정 요청 DTO")
    public static class Update {

        @Valid
        @NotNull(message = "루틴 목록은 필수입니다.")
        @Size(min = 1, message = "루틴은 최소 1개 이상 등록해야 합니다.")
        @Schema(description = "수정할 상세 루틴 정보 리스트")
        private List<RoutineData> routines;

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @ToString
        @Schema(description = "수정할 개별 상세 루틴 정보")
        public static class RoutineData {

            @NotNull(message = "루틴 ID는 필수입니다.")
            @Schema(description = "수정할 루틴 ID", example = "1")
            private Long routineId;

            @Schema(description = "연결된 템플릿 ID (선택)", example = "1")
            private Long templateId;

            @NotNull(message = "이모지 ID는 필수입니다.")
            @Schema(description = "선택한 이모지 ID", example = "1")
            private Long emojiId;

            @NotBlank(message = "루틴 이름은 필수입니다.")
            @Size(max = 50, message = "루틴 이름은 최대 50자까지 입력 가능합니다.")
            @Schema(description = "루틴 이름", example = "운동 하기")
            private String name;

            @NotNull(message = "소요 시간은 필수입니다.")
            @Min(value = 1, message = "소요 시간은 1분 이상이어야 합니다.")
            @Max(value = 999, message = "소요 시간은 999분 이하여야 합니다.")
            @Schema(description = "루틴 소요 시간(분)", example = "120")
            private Integer time;
        }
    }

    /**
     * <h3>상세 루틴 상태 변경 요청 DTO</h3>
     * <p>PATCH /api/v1/routines/groups/{groupRoutineListId}/status/{routineId}</p>
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @Schema(description = "상세 루틴 상태(성공/실패) 변경 요청 DTO")
    public static class StatusUpdate {

        @NotNull(message = "상태 값은 필수입니다.")
        @Schema(description = "루틴 완료 여부 (true: 성공, false: 실패)", example = "true")
        private Boolean status;
    }
}
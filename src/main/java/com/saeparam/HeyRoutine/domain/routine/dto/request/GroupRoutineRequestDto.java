package com.saeparam.HeyRoutine.domain.routine.dto.request;

import com.saeparam.HeyRoutine.domain.routine.enums.RoutineType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * <h2>GroupRoutineRequestDto</h2>
 * <p>
 * 단체 루틴(Group Routine) 관련 API의 <b>요청(Request)</b> 데이터 전송을 담당하는 DTO 클래스들을 포함<br>
 * 각 기능에 최적화된 요청 클래스를 내부 정적 클래스로 정의해서 응집도를 높였습니다.
 * </p>
 *
 * <pre>
 * ┌───────────────────────────────┐
 * │ GroupRoutineRequestDto        │
 * ├───────────────────────────────┤
 * │ - Create                      │──► 단체 루틴 생성 요청
 * │ - Update                      │──► 단체 루틴 수정 요청
 * │ - RecordUpdate                │──► 단체 루틴 완료 여부 갱신 요청
 * └───────────────────────────────┘
 * </pre>
 */
public class GroupRoutineRequestDto {

    /**
     * <h3>[Request] 단체 루틴 생성</h3>
     * <p>POST /api/v1/routines/groups</p>
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @Schema(description = "단체 루틴 생성 요청 DTO")
    public static class Create {

        @NotBlank(message = "타이틀은 필수 입력 항목입니다.")
        @Size(max = 50, message = "타이틀은 최대 50자까지 입력 가능합니다.")
        @Schema(description = "단체 루틴 타이틀", example = "아이고 종강이야")
        private String title;

        @NotNull(message = "설명은 필수 입력 항목입니다.")
        @Size(max = 200, message = "설명은 최대 200자까지 입력 가능합니다.")
        @Schema(description = "단체 루틴 설명", example = "종강까지 아침 갓생 루틴 필요하신 분 들어오셈")
        private String description;

        @NotNull(message = "루틴 타입은 필수 입력 항목입니다.")
        @Schema(description = "루틴 타입 (DAILY: 일상, FINANCE: 소비)", example = "DAILY")
        private RoutineType routineType;

        @NotNull(message = "요일은 필수 선택 항목입니다.")
        @Size(min = 1, message = "요일은 최소 1개 이상 선택해야 합니다.")
        @Schema(description = "루틴 수행 요일 리스트", example = "[\"월\", \"수\", \"금\"]")
        private List<String> daysOfWeek;

        @NotBlank(message = "시작 시간은 필수 입력 항목입니다.")
        @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "시간 형식을 HH:mm에 맞게 입력해주세요.")
        @Schema(description = "루틴 시작 시간 (HH:mm)", example = "08:00")
        private String startTime;

        @NotBlank(message = "종료 시간은 필수 입력 항목입니다.")
        @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "시간 형식을 HH:mm에 맞게 입력해주세요.")
        @Schema(description = "루틴 종료 시간 (HH:mm)", example = "09:00")
        private String endTime;
    }

    /**
     * <h3>[Request] 단체 루틴 수정</h3>
     * <p>PUT /api/v1/routines/groups/{groupRoutineListId}</p>
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @Schema(description = "단체 루틴 수정 요청 DTO")
    public static class Update {

        @NotBlank(message = "타이틀은 필수 입력 항목입니다.")
        @Size(max = 50, message = "타이틀은 최대 50자까지 입력 가능합니다.")
        @Schema(description = "단체 루틴 타이틀", example = "아이고 종강이야")
        private String title;

        @NotNull(message = "설명은 필수 입력 항목입니다.")
        @Size(max = 200, message = "설명은 최대 200자까지 입력 가능합니다.")
        @Schema(description = "단체 루틴 설명", example = "종강까지 아침 갓생 루틴 필요하신 분 들어오셈")
        private String description;

        @NotNull(message = "루틴 타입은 필수 입력 항목입니다.")
        @Schema(description = "루틴 타입 (DAILY: 일상, FINANCE: 소비)", example = "DAILY")
        private RoutineType routineType;

        @NotNull(message = "요일은 필수 선택 항목입니다.")
        @Size(min = 1, message = "요일은 최소 1개 이상 선택해야 합니다.")
        @Schema(description = "루틴 수행 요일 리스트", example = "[\"월\", \"수\", \"금\"]")
        private List<String> daysOfWeek;

        @NotBlank(message = "시작 시간은 필수 입력 항목입니다.")
        @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "시간 형식을 HH:mm에 맞게 입력해주세요.")
        @Schema(description = "루틴 시작 시간 (HH:mm)", example = "08:00")
        private String startTime;

        @NotBlank(message = "종료 시간은 필수 입력 항목입니다.")
        @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "시간 형식을 HH:mm에 맞게 입력해주세요.")
        @Schema(description = "루틴 종료 시간 (HH:mm)", example = "09:00")
        private String endTime;
    }

    /**
     * <h3>[Request] 단체 루틴 완료 여부 갱신</h3>
     * <p>PATCH /api/v1/routines/groups/{groupRoutineListId}</p>
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @Schema(description = "단체 루틴 완료 여부 갱신 요청 DTO")
    public static class RecordUpdate {

        @NotNull(message = "상태 값은 필수입니다.")
        @Schema(description = "단체 루틴 완료 여부 (true: 성공, false: 실패)", example = "true")
        private Boolean status;
    }
}
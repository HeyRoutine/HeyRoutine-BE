package com.saeparam.HeyRoutine.domain.routine.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * <h2>GuestbookRequestDto</h2>
 * <p>
 * 방명록(Guestbook) 관련 API의 <b>요청(Request)</b> 데이터 전송을 담당하는 DTO 클래스들을 포함합니다.<br>
 * 각 기능에 최적화된 요청 클래스를 내부 정적 클래스로 정의하여 응집도를 높였습니다.
 * </p>
 *
 * <pre>
 * ┌───────────────────────────┐
 * │ GuestbookRequestDto       │
 * ├───────────────────────────┤
 * │ - Create                  │──► 방명록 생성 요청
 * └───────────────────────────┘
 * </pre>
 */
public class GuestbookRequestDto {

    private GuestbookRequestDto() {
        // 유틸리티 클래스로, 인스턴스화 방지
    }

    /**
     * <h3>방명록 생성 요청 DTO</h3>
     * <p>
     *     POST /api/v1/routines/groups/{groupRoutineListId}/guestbooks
     * </p>
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @Schema(description = "방명록 생성 요청 DTO")
    public static class Create {

        @NotBlank(message = "방명록 내용은 필수 입력 항목입니다.")
        @Size(max = 500, message = "방명록 내용은 최대 500자까지 입력 가능합니다.")
        @Schema(description = "방명록 내용", example = "좋은 루틴 만들어주셔서 감사합니다!")
        private String content;
    }
}
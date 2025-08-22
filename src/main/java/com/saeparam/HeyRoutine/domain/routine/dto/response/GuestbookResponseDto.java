package com.saeparam.HeyRoutine.domain.routine.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

/**
 * <h2>GuestbookResponseDto</h2>
 * <p>
 * 방명록(Guestbook) 관련 API의 <b>응답(Response)</b> 데이터 전송을 담당하는 DTO 클래스들을 포함합니다.<br>
 * 각 기능에 최적화된 응답 클래스를 내부 정적 클래스로 정의하여 응집도를 높였습니다.
 * </p>
 *
 * <pre>
 * ┌───────────────────────────┐
 * │ GuestbookResponseDto      │
 * ├───────────────────────────┤
 * │ - GuestbookInfo           │──► 방명록 단일 정보 (생성/조회 시 사용)
 * │ - GuestbookList           │──► 방명록 목록 정보
 * └───────────────────────────┘
 * </pre>
 */
public class GuestbookResponseDto {

    private GuestbookResponseDto() {
        // 유틸리티 클래스로, 인스턴스화 방지
    }

    /**
     * <h3>방명록 정보 DTO</h3>
     * <p>방명록 생성 및 조회 시, 개별 방명록의 상세 정보를 담는 DTO입니다.</p>
     */
    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "방명록 정보 DTO")
    public static class GuestbookInfo {

        @Schema(description = "방명록 ID", example = "1")
        private Long id;

        @Schema(description = "작성자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        private UUID userId;

        @Schema(description = "작성자 닉네임", example = "이싸피")
        private String nickname;

        @Schema(description = "작성자 프로필 이미지 URL", example = "https://example.com/profile/123.jpg")
        private String profileImageUrl;

        @Schema(description = "방명록 내용", example = "좋은 루틴 만들어주셔서 감사합니다!")
        private String content;

        @Schema(description = "방명록 작성 시간 (yyyy-MM-dd HH:mm:ss)", example = "2025-08-20 15:30:45")
        private String createdAt;

        @Schema(description = "현재 사용자가 작성자인지 여부", example = "false")
        private Boolean isWriter;
    }

    /**
     * <h3>방명록 목록 조회 응답 DTO</h3>
     * <p>
     *     GET /api/v1/routines/groups/{groupRoutineListId}/guestbooks
     * </p>
     */
    @Getter
    @Builder
    @Schema(description = "방명록 목록 조회 응답 DTO")
    public static class GuestbookList {

        @Schema(description = "방명록 정보 리스트")
        private List<GuestbookInfo> items;
    }
}
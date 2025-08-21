package com.saeparam.HeyRoutine.domain.routine.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * <h2>CommonResponseDto</h2>
 * <p>
 * 여러 도메인에서 공통으로 사용되는 API의 <b>응답(Response)</b> 데이터 전송을 담당하는 DTO 클래스들을 포함합니다.<br>
 * 각 기능에 최적화된 응답 클래스를 내부 정적 클래스로 정의하여 응집도를 높였습니다.
 * </p>
 *
 * <pre>
 * ┌───────────────────────────┐
 * │ CommonResponseDto         │
 * ├───────────────────────────┤
 * │ - EmojiInfo               │──► 이모지 단일 정보
 * │ - EmojiList               │──► 이모지 목록 정보
 * │ - TemplateInfo            │──► 템플릿 단일 정보
 * │ - TemplateList            │──► 템플릿 목록 정보
 * └───────────────────────────┘
 * </pre>
 */
public class CommonResponseDto {

    private CommonResponseDto() {
        // 유틸리티 클래스로, 인스턴스화 방지
    }

    /**
     * <h3>이모지 정보 DTO</h3>
     * <p>이모지 목록 조회 시, 개별 이모지의 정보를 담는 DTO입니다.</p>
     */
    @Getter
    @Builder
    @Schema(description = "이모지 정보 DTO")
    public static class EmojiInfo {

        @Schema(description = "이모지 ID", example = "1")
        private Long emojiId;

        @Schema(description = "이모지 이미지 URL", example = "https://example.com/emoji.png")
        private String emojiUrl;
    }

    /**
     * <h3>이모지 목록 조회 응답 DTO</h3>
     * <p>
     *     GET /api/v1/routines/emoji
     * </p>
     */
    @Getter
    @Builder
    @Schema(description = "이모지 목록 조회 응답 DTO")
    public static class EmojiList {

        @Schema(description = "이모지 정보 리스트")
        private List<EmojiInfo> items;
    }

    /**
     * <h3>루틴 템플릿 정보 DTO</h3>
     * <p>루틴 템플릿 목록 조회 시, 개별 템플릿의 정보를 담는 DTO입니다.</p>
     */
    @Getter
    @Builder
    @Schema(description = "루틴 템플릿 정보 DTO")
    public static class TemplateInfo {

        @Schema(description = "템플릿 ID", example = "1")
        private Long templateId;

        @Schema(description = "이모지 ID", example = "1")
        private Long emojiId;

        @Schema(description = "템플릿 이름", example = "커피 내리기")
        private String name;

        @Schema(description = "템플릿 설명", example = "아침에 커피를 내려서 마시는 시간")
        private String content;
    }

    /**
     * <h3>루틴 템플릿 목록 조회 응답 DTO</h3>
     * <p>
     *     GET /api/v1/routines/templates
     * </p>
     */
    @Getter
    @Builder
    @Schema(description = "루틴 템플릿 목록 조회 응답 DTO")
    public static class TemplateList {

        @Schema(description = "루틴 템플릿 정보 리스트")
        private List<TemplateInfo> items;
    }
}
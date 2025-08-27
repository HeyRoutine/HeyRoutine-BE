package com.saeparam.HeyRoutine.domain.analysis.dto.request;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Gemini API 요청 DTO
 */
@Getter
@NoArgsConstructor
public class GeminiReqDto {
    private List<Content> contents;

    public GeminiReqDto(String text) {
        this.contents = new ArrayList<>();
        this.contents.add(new Content(text));
    }

    @Getter
    @NoArgsConstructor
    public static class Content {
        private List<Part> parts;

        public Content(String text) {
            this.parts = new ArrayList<>();
            this.parts.add(new Part(text));
        }

        @Getter
        @NoArgsConstructor
        public static class Part {
            private String text;

            public Part(String text) {
                this.text = text;
            }
        }
    }
}
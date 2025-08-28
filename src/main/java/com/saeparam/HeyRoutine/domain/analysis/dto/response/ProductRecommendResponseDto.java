package com.saeparam.HeyRoutine.domain.analysis.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.saeparam.HeyRoutine.domain.analysis.dto.ResultDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor // JSON 역직렬화를 위해 기본 생성자 필요
public class ProductRecommendResponseDto {
    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("top_k")
    private int topK;

    private List<ResultDto> results;
}
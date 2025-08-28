package com.saeparam.HeyRoutine.domain.analysis.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.saeparam.HeyRoutine.domain.analysis.dto.TransactionDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProductRecommendRequestDto {
    @JsonProperty("user_id") // JSON key를 snake_case로 매핑
    private String userId;

    private List<TransactionDto> transactions;

    @JsonProperty("top_k")
    private int topK;
}
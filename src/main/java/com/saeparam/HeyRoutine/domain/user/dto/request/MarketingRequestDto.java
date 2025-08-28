package com.saeparam.HeyRoutine.domain.user.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 마케팅 수신 동의 여부 업데이트 요청 DTO.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketingRequestDto {
    @JsonProperty("status")
    private boolean isMarketing;
}
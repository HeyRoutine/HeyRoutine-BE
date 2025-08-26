package com.saeparam.HeyRoutine.domain.finance.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 1원 송금 검증 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CheckAuthCodeResponseDto {
    @JsonProperty("REC")
    private Rec rec;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Rec {
        private String status;
        private String transactionUniqueNo;
        private String accountNo;
    }
}
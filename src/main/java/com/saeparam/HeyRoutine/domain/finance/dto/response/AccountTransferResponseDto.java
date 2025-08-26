package com.saeparam.HeyRoutine.domain.finance.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 계좌 입출금 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountTransferResponseDto {
    @JsonProperty("REC")
    private Rec rec;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Rec {
        private String transactionUniqueNo;
        private String transactionDate;
    }
}
package com.saeparam.HeyRoutine.domain.finance.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 계좌 거래내역 단건 조회 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistoryResponseDto {
    @JsonProperty("REC")
    private Rec rec;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Rec {
        private String transactionUniqueNo;
        private String transactionDate;
        private String transactionTime;
        private String transactionType;
        private String transactionTypeName;
        private String transactionAccountNo;
        private String transactionBalance;
        private String transactionAfterBalance;
        private String transactionSummary;
        private String transactionMemo;
    }
}
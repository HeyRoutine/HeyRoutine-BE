package com.saeparam.HeyRoutine.domain.finance.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.saeparam.HeyRoutine.domain.user.dto.request.BankAccountHeaderDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 계좌 거래내역 리스트 조회 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistoryListRequestDto {
    @JsonProperty("Header")
    private BankAccountHeaderDto header;
    private String accountNo;
    private String startDate;
    private String endDate;
    private String transactionType;
    private String orderByType;
}
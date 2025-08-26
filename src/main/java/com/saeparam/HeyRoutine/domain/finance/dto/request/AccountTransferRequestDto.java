package com.saeparam.HeyRoutine.domain.finance.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.saeparam.HeyRoutine.domain.user.dto.request.BankAccountHeaderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 계좌 입출금 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountTransferRequestDto {
    @JsonProperty("Header")
    private BankAccountHeaderDto header;
    private String accountNo;
    private String transactionBalance;
    private String transactionSummary;
}
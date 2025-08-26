package com.saeparam.HeyRoutine.domain.finance.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.saeparam.HeyRoutine.domain.user.dto.request.BankAccountHeaderDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 1원 송금 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OpenAccountAuthRequestDto {
    @JsonProperty("Header")
    private BankAccountHeaderDto header;
    private String accountNo;
    private String authText;
}
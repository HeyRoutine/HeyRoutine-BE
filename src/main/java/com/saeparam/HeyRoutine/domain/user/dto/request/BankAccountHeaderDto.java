package com.saeparam.HeyRoutine.domain.user.dto.request;

import lombok.Builder;
import lombok.Getter;

// JSON의 Header 객체와 매핑될 DTO
@Getter
@Builder
public class BankAccountHeaderDto {
    private String apiName;
    private String transmissionDate;
    private String transmissionTime;
    private String institutionCode;
    private String fintechAppNo;
    private String apiServiceCode;
    private String institutionTransactionUniqueNo;
    private String apiKey;
    private String userKey;
}
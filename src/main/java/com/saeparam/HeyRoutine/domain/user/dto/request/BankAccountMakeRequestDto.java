package com.saeparam.HeyRoutine.domain.user.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 전체 요청 본문(Body)과 매핑될 최상위 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountMakeRequestDto {

    // 실제 JSON의 키가 대문자 'Header'이므로 @JsonProperty로 정확히 매핑합니다.
    @JsonProperty("Header")
    private BankAccountHeaderDto header;

    private String accountTypeUniqueNo;
}
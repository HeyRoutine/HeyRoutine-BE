package com.saeparam.HeyRoutine.domain.finance.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 계좌 인증번호 검증 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountCodeVerifyRequestDto {
    /** 사용자가 입력한 인증번호 */
    private String code;
}

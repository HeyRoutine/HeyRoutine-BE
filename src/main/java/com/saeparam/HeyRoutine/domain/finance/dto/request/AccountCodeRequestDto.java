package com.saeparam.HeyRoutine.domain.finance.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 계좌 인증번호 발송 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountCodeRequestDto {
    /** 계좌번호 */
    private String account;
}
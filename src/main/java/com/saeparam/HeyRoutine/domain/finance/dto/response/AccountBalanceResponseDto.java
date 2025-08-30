package com.saeparam.HeyRoutine.domain.finance.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 계좌 잔액 조회 응답 DTO
 */
@Getter
@NoArgsConstructor
public class AccountBalanceResponseDto {
	@JsonProperty("REC")
	private Rec rec;

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Rec {
		private String bankCode;
		private String accountNo;
		private String accountBalance;
		private String accountCreatedDate;
		private String accountExpiryDate;
		private String lastTransactionDate;
		private String currency;
	}
}
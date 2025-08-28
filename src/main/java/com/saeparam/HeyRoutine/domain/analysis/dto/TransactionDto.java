package com.saeparam.HeyRoutine.domain.analysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.saeparam.HeyRoutine.domain.finance.dto.response.TransactionHistoryListResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
public class TransactionDto {
    @JsonProperty("user_id")
    private String userId;

    private String ts; // Timestamp or Date String

    private String transactionType;

    private long transactionBalance;

    private long transactionAfterBalance;

    // TransactionHistoryListResponseDto.History 객체를 이 DTO로 변환하는 정적 메소드
    public static TransactionDto from(TransactionHistoryListResponseDto.History history, String userId) {
        return TransactionDto.builder()
                .userId(userId)
                .ts(history.getTransactionDate()) // yyyy-MM-dd 형식의 날짜 문자열
                .transactionType(history.getTransactionType())
                .transactionBalance(Long.parseLong(history.getTransactionBalance().replace(",", "")))
                .transactionAfterBalance(Long.parseLong(history.getTransactionAfterBalance().replace(",", "")))
                .build();
    }
}
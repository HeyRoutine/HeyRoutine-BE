package com.saeparam.HeyRoutine.domain.finance.service;

import com.saeparam.HeyRoutine.domain.finance.dto.response.TransactionHistoryListResponseDto;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.time.LocalDate;

public interface FinanceService {

    /**
     * 계좌로 1원을 송금하고 인증번호를 FCM으로 전송한다.
     *
     * @param userId    사용자 ID
     * @param accountNo 계좌번호
     */
    void sendAccountCode(UUID userId, String accountNo);

    /**
     * 사용자가 입력한 인증번호를 검증한다.
     *
     * @param userId 사용자 ID
     * @param code   인증번호
     * @return 인증 성공 여부
     */
    boolean verifyAccountCode(UUID userId, String code);

    /**
     * 더미 입출금 거래를 생성한다.
     *
     * @param userKey   은행 사용자 키
     * @param accountNo 계좌번호
     */
    Mono<Void> generateDummyTransactions(String userKey, String accountNo);

    /**
     * 특정 기간의 거래내역을 조회한다.
     *
     * @param userId 사용자 ID
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 거래내역 응답 DTO
     */
    TransactionHistoryListResponseDto getTransactionHistoryList(UUID userId, LocalDate startDate, LocalDate endDate);
}

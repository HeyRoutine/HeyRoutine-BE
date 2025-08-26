package com.saeparam.HeyRoutine.domain.finance.service;

import java.util.UUID;

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
}

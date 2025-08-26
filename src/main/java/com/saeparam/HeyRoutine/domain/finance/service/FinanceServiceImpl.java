package com.saeparam.HeyRoutine.domain.finance.service;

import com.saeparam.HeyRoutine.domain.finance.dto.response.CheckAuthCodeResponseDto;
import com.saeparam.HeyRoutine.domain.finance.dto.response.OpenAccountAuthResponseDto;
import com.saeparam.HeyRoutine.domain.finance.dto.response.TransactionHistoryResponseDto;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import com.saeparam.HeyRoutine.domain.user.repository.UserRepository;
import com.saeparam.HeyRoutine.global.infra.http.bank.WebClientBankUtil;
import com.saeparam.HeyRoutine.global.infra.messaging.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FinanceServiceImpl {
    private final UserRepository userRepository;
    private final WebClientBankUtil webClientBankUtil;
    private final FcmService fcmService;

    private static final String AUTH_TEXT = "헤이루틴"; // 거래 요약에 표시될 기업명

    /**
     * 계좌로 1원을 송금하고 인증번호를 FCM으로 전송한다.
     *
     * @param userId    사용자 ID
     * @param accountNo 계좌번호
     */
    @Transactional
    public void sendAccountCode(UUID userId, String accountNo) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 1원 송금 요청
        OpenAccountAuthResponseDto authResponse = webClientBankUtil
                .openAccountAuth(user.getUserKey(), accountNo, AUTH_TEXT)
                .block();
        if (authResponse == null || authResponse.getRec() == null) {
            throw new IllegalArgumentException("유효하지 않은 계좌번호입니다.");
        }

        String transactionUniqueNo = authResponse.getRec().getTransactionUniqueNo();

        // 거래내역 조회 후 인증번호 추출
        TransactionHistoryResponseDto historyResponse = webClientBankUtil
                .inquireTransactionHistory(user.getUserKey(), accountNo, transactionUniqueNo)
                .block();
        if (historyResponse == null || historyResponse.getRec() == null) {
            throw new IllegalArgumentException("유효하지 않은 계좌번호입니다.");
        }
        String summary = historyResponse.getRec().getTransactionSummary();
        String authCode = extractAuthCode(summary);

        // 계좌번호 저장
        user.setBankAccount(accountNo);

        // FCM으로 인증번호 전송
        fcmService.sendAccountAuthCode(user.getId(), authCode);
    }

    /**
     * 사용자가 입력한 인증번호를 검증한다.
     *
     * @param userId 사용자 ID
     * @param code   인증번호
     * @return 인증 성공 여부
     */
    @Transactional
    public boolean verifyAccountCode(UUID userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        CheckAuthCodeResponseDto response = webClientBankUtil
                .checkAuthCode(user.getUserKey(), user.getBankAccount(), AUTH_TEXT, code)
                .block();
        if (response != null && response.getRec() != null && "SUCCESS".equals(response.getRec().getStatus())) {
            user.setAccountCertificationStatus(true);
            return true;
        }
        return false;
    }

    /**
     * 거래 요약 문자열에서 인증번호만 추출한다.
     */
    private String extractAuthCode(String summary) {
        if (summary == null) {
            return "";
        }
        String[] parts = summary.split(" ");
        if (parts.length >= 2) {
            return parts[1];
        }
        return "";
    }
}

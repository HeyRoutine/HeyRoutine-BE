package com.saeparam.HeyRoutine.domain.finance.service;

import com.saeparam.HeyRoutine.domain.finance.dto.response.CheckAuthCodeResponseDto;
import com.saeparam.HeyRoutine.domain.finance.dto.response.OpenAccountAuthResponseDto;
import com.saeparam.HeyRoutine.domain.finance.dto.response.TransactionHistoryResponseDto;
import com.saeparam.HeyRoutine.domain.finance.template.DepositTemplates;
import com.saeparam.HeyRoutine.domain.finance.template.ExpenseTemplates;
import com.saeparam.HeyRoutine.domain.finance.template.TransactionTemplate;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import com.saeparam.HeyRoutine.domain.user.repository.UserRepository;
import com.saeparam.HeyRoutine.global.infra.http.bank.WebClientBankUtil;
import com.saeparam.HeyRoutine.global.infra.messaging.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FinanceServiceImpl implements FinanceService{
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
    @Override
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
    @Override
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

    /**
     * 계좌에 더미 입출금 내역을 생성한다.
     */
    @Override
    public void generateDummyTransactions(String userKey, String accountNo) {
        List<TransactionTemplate> deposits = new ArrayList<>(DepositTemplates.TEMPLATES);
        Collections.shuffle(deposits);
        long totalDeposit = 0L;

        for (TransactionTemplate template : deposits.subList(0, 2)) {
            try {
                webClientBankUtil.deposit(userKey, accountNo, template.amount(), template.summary())
                        .doOnSuccess(v -> log.debug("입금 성공: {}", template.summary()))
                        .doOnError(e -> log.error("입금 실패: {}", e.getMessage()))
                        .block();
                totalDeposit += template.amount();
            } catch (Exception e) {
                log.error("입금 처리 중 오류", e);
            }
        }

        long remaining = totalDeposit;
        List<TransactionTemplate> expenses = new ArrayList<>(ExpenseTemplates.TEMPLATES);
        Collections.shuffle(expenses);
        int count = 0;
        for (TransactionTemplate template : expenses) {
            if (count >= 10) {
                break;
            }
            if (template.amount() > remaining) {
                continue;
            }
            try {
                webClientBankUtil.withdraw(userKey, accountNo, template.amount(), template.summary())
                        .doOnSuccess(v -> log.debug("출금 성공: {}", template.summary()))
                        .doOnError(e -> log.error("출금 실패: {}", e.getMessage()))
                        .block();
                remaining -= template.amount();
                count++;
            } catch (Exception e) {
                log.error("출금 처리 중 오류", e);
            }
        }
    }
}

package com.saeparam.HeyRoutine.domain.user.service.event;

import com.saeparam.HeyRoutine.domain.user.dto.request.BankUserMakeRequestDto;
import com.saeparam.HeyRoutine.domain.user.dto.response.BankAccountResponseDto;
import com.saeparam.HeyRoutine.domain.user.dto.response.BankUserMakeResponseDto;
import com.saeparam.HeyRoutine.global.infra.http.bank.WebClientBankUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
class UserSignupListener {

    private final WebClientBankUtil webClientBankUtil;
    private final UserAccountUpdater userAccountUpdater;

    /**
     * UserSignedUpEvent가 발행되면, 회원가입 트랜잭션이 '커밋된 후에' 이 메서드가 실행됩니다.
     * 이를 통해 Race Condition을 방지합니다.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async

    public void handleUserSignedUp(UserSignedUpEvent event) {

        log.info("회원가입 트랜잭션 커밋 확인. 비동기 계좌 생성을 시작합니다. email: {}", event.getEmail());
        bankAccountMake(event.getEmail());
    }

    /**
     * 비동기 API를 호출하여 계좌를 생성하고, 결과를 DB에 저장하도록 요청합니다.
     */
    public void bankAccountMake(String email){
        String accountTypeUniqueNo = "001-1-027c9c26b2d247";

        webClientBankUtil.makeUserAccount(email, BankUserMakeRequestDto.class, BankUserMakeResponseDto.class)
                .flatMap(userResponse -> {
                    String userKey = userResponse.getUserKey();
                    userAccountUpdater.updateUserKey(email, userKey);

                    log.info("은행 계정 생성 성공! UserKey: {}", userKey);
                    return webClientBankUtil.createDemandDepositAccount(
                            userKey,
                            accountTypeUniqueNo,
                            BankAccountResponseDto.class
                    );
                })
                .subscribe(
                        accountResponse -> {
                            String accountNumber = accountResponse.getRec().getAccountNo();
                            log.info("요구불 계좌 생성 성공! 계좌번호: {}", accountNumber);
                            // 별도의 트랜잭션을 가진 서비스에 DB 업데이트를 위임합니다.
                            userAccountUpdater.updateUserBankAccount(email, accountNumber);
                        },
                        error -> {
                            log.error("은행 계좌 생성 프로세스 중 에러 발생: {}", error.getMessage());
                            // TODO: 실패 시 재시도 로직(e.g., 메시지 큐 사용) 또는 관리자 알림 등 후속 처리
                        }
                );
    }
}

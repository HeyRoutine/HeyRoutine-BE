package com.saeparam.HeyRoutine.domain.user.service.event;

import com.saeparam.HeyRoutine.domain.user.entity.User;
import com.saeparam.HeyRoutine.domain.user.repository.UserRepository;
import com.saeparam.HeyRoutine.global.error.handler.UserHandler;
import com.saeparam.HeyRoutine.global.web.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
class UserAccountUpdater {

    private final UserRepository userRepository;

    /**
     * 사용자의 계좌번호를 DB에 저장합니다.
     * 이 메서드는 자체적인 쓰기 가능 트랜잭션으로 실행됩니다.
     */
    @Transactional
    public void updateUserBankAccount(String email, String accountNumber) {
        // 이 시점에는 이미 사용자가 DB에 확실히 존재하므로 안전하게 조회할 수 있습니다.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        
        user.setBankAccount(accountNumber);
        
        // @Transactional에 의해 메서드 종료 시 변경된 내용이 자동으로 DB에 반영됩니다.
        log.info("{} 사용자의 계좌번호가 DB에 저장되었습니다.", email);
    }
}

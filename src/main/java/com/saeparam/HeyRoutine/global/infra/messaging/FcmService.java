package com.saeparam.HeyRoutine.global.infra.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * FCM 메시지 전송을 담당하는 서비스.
 * <p>실제 Firebase 연동은 추후 구현될 수 있습니다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    /**
     * 계좌 인증번호를 사용자에게 전송합니다.
     *
     * @param userId   사용자 ID
     * @param authCode 인증번호
     */
    public void sendAccountAuthCode(UUID userId, String authCode) {
        // 실제 FCM 연동 대신 로그로 대체합니다.
        log.info("[FCM] Send auth code {} to user {}", authCode, userId);
    }
}
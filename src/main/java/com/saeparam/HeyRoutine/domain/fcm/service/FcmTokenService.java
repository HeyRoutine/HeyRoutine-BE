package com.saeparam.HeyRoutine.domain.fcm.service;


import com.saeparam.HeyRoutine.domain.fcm.dto.FcmTokenRequestDto;
import com.saeparam.HeyRoutine.domain.fcm.entity.FcmToken;
import com.saeparam.HeyRoutine.domain.fcm.repository.FcmTokenRepository;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import com.saeparam.HeyRoutine.domain.user.repository.UserRepository;
import com.saeparam.HeyRoutine.global.error.handler.UserHandler;
import com.saeparam.HeyRoutine.global.web.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmTokenService {
    private final UserRepository userRepository;
    private final FcmTokenRepository fcmTokenRepository;

    @Transactional
    public String saveToken(UUID userId, FcmTokenRequestDto fcmTokenRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        fcmTokenRepository.save(FcmToken.builder()
                        .token(fcmTokenRequestDto.getFcmToken())
                        .user(user)
                .build());
        return "토큰이 저장되었습니다.";
    }
}

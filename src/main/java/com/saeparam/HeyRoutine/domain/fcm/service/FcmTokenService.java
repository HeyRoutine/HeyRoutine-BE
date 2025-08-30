package com.saeparam.HeyRoutine.domain.fcm.service;


import com.saeparam.HeyRoutine.domain.fcm.dto.FcmTokenRequestDto;
import com.saeparam.HeyRoutine.domain.fcm.entity.FcmToken;
import com.saeparam.HeyRoutine.domain.fcm.repository.FcmTokenRepository;
import com.saeparam.HeyRoutine.domain.shop.dto.request.ExpoPushRequestDto;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import com.saeparam.HeyRoutine.domain.user.repository.UserRepository;
import com.saeparam.HeyRoutine.global.error.handler.UserHandler;
import com.saeparam.HeyRoutine.global.web.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmTokenService {
    private final UserRepository userRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final WebClient expoWebClient; // WebClient Bean 주입 (아래 설정 참고)

    @Transactional
    public String saveToken(UUID userId, FcmTokenRequestDto fcmTokenRequestDto) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 해당 사용자의 기존 FCM 토큰이 있는지 조회
        Optional<FcmToken> existingToken = fcmTokenRepository.findByUser(user);

        if (existingToken.isPresent()) {
            // 3-1. 토큰이 이미 존재하면 -> 토큰 값 업데이트 (더티 체킹 활용)
            FcmToken fcmTokenToUpdate = existingToken.get();
            fcmTokenToUpdate.updateToken(fcmTokenRequestDto.getFcmToken());
            return "토큰이 성공적으로 업데이트되었습니다.";
        } else {
            // 3-2. 토큰이 없으면 -> 새로 생성 및 저장
            FcmToken newFcmToken = FcmToken.builder()
                    .token(fcmTokenRequestDto.getFcmToken())
                    .user(user)
                    .build();
            fcmTokenRepository.save(newFcmToken);
            return "토큰이 성공적으로 저장되었습니다.";
        }
    }


    public Mono<String> sendPushNotification(String expoPushToken, String title, String body) {
        String expoPushUrl = "https://exp.host/--/api/v2/push/send";

        ExpoPushRequestDto requestDto = ExpoPushRequestDto.builder()
                .to(Collections.singletonList(expoPushToken))
                .sound("default")
                .title(title)
                .body(body)
                .build();

        return expoWebClient.post()
                .uri(expoPushUrl)
                .bodyValue(requestDto)
                .retrieve()
                .onStatus(
                        httpStatus -> httpStatus.isError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Expo Push API Error: Status={}, Body={}", clientResponse.statusCode(), errorBody);
                                    return Mono.error(new RuntimeException("Push API 호출에 실패했습니다: " + errorBody));
                                })
                )
                .bodyToMono(String.class) // 성공 응답 Body를 String으로 받음
                .doOnSuccess(responseBody -> log.info("Expo Push API 성공: {}", responseBody));
    }


}

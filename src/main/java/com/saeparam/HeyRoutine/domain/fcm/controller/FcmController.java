package com.saeparam.HeyRoutine.domain.fcm.controller;

import com.saeparam.HeyRoutine.domain.fcm.dto.FcmTokenRequestDto;
import com.saeparam.HeyRoutine.domain.fcm.service.FcmTokenService;
import com.saeparam.HeyRoutine.global.security.jwt.JwtTokenProvider;
import com.saeparam.HeyRoutine.global.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/fcm")

public class FcmController {
    private final JwtTokenProvider jwtTokenProvider;
    private final FcmTokenService fcmTokenService;



    @PostMapping("/token")
    @Operation(summary = "fcm 토큰 저장API", description = "해당유저의 fcm토큰을 저장합니다")
    public ResponseEntity<?> accountVerification(@RequestHeader("Authorization") String token, @RequestBody FcmTokenRequestDto fcmTokenRequestDto) {
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));

        return ResponseEntity.ok().body(ApiResponse.onSuccess(fcmTokenService.saveToken(userId,fcmTokenRequestDto)));

    }
}

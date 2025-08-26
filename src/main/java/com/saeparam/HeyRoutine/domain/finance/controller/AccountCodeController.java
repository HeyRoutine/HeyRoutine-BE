package com.saeparam.HeyRoutine.domain.finance.controller;

import com.saeparam.HeyRoutine.domain.finance.dto.request.AccountCodeRequestDto;
import com.saeparam.HeyRoutine.domain.finance.dto.request.AccountCodeVerifyRequestDto;
import com.saeparam.HeyRoutine.domain.finance.service.FinanceServiceImpl;
import com.saeparam.HeyRoutine.global.security.jwt.JwtTokenProvider;
import com.saeparam.HeyRoutine.global.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 계좌 인증번호 발송 및 검증 API 컨트롤러
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/accountCode")
public class AccountCodeController {

    private final JwtTokenProvider jwtTokenProvider;
    private final FinanceServiceImpl financeService;

    @PostMapping
    @Operation(summary = "계좌 인증번호 전송 API", description = "1원 송금 후 인증번호를 FCM으로 전송합니다.")
    public ResponseEntity<?> sendAccountCode(@RequestHeader("Authorization") String token,
                                             @RequestBody AccountCodeRequestDto requestDto) {
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        try {
            financeService.sendAccountCode(userId, requestDto.getAccount());
            return ResponseEntity.ok(ApiResponse.onSuccess("해당 계좌는 유효합니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.onFailure("COMMON404", "유효하지 않은 계좌번호입니다."));
        }
    }

    @PostMapping("/verify")
    @Operation(summary = "계좌 인증번호 인증 API", description = "1원 송금 인증번호를 검증합니다.")
    public ResponseEntity<?> verifyAccountCode(@RequestHeader("Authorization") String token,
                                               @RequestBody AccountCodeVerifyRequestDto requestDto) {
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        boolean result = financeService.verifyAccountCode(userId, requestDto.getCode());
        if (result) {
            return ResponseEntity.ok(ApiResponse.onSuccess("인증 성공"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.onFailure("COMMON404", "인증번호가 틀렸거나 올바르지 않습니다."));
    }
}
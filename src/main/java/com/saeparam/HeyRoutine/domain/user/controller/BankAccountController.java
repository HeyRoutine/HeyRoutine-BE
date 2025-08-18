package com.saeparam.HeyRoutine.domain.user.controller;

import com.saeparam.HeyRoutine.domain.user.service.BankAccountService;
import com.saeparam.HeyRoutine.global.security.jwt.JwtTokenProvider;
import com.saeparam.HeyRoutine.global.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/account")

public class BankAccountController {
    private final JwtTokenProvider jwtTokenProvider;
    private final BankAccountService bankAccountService;
    @PatchMapping("/registration")
    @Operation(summary = "계좌등록 API", description = "계좌를 등록합니다.")
    public ResponseEntity<?> accountRegistration(@RequestHeader("Authorization") String token,@RequestParam("account") String account) {
        String email = jwtTokenProvider.getEmail(token.substring(7));
        String result=bankAccountService.accountRegistration(token,account);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(result));
    }

    @PostMapping("/verification")
    @Operation(summary = "계좌인증 API", description = "1원 계좌인증을 합니다.")
    public ResponseEntity<?> accountVerification(@RequestHeader("Authorization") String token,@RequestParam("account") String account) {
        String email = jwtTokenProvider.getEmail(token.substring(7));
        String result=bankAccountService.accountVerification(token,account);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(result));
    }
}

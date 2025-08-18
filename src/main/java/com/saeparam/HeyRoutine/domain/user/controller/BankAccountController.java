package com.saeparam.HeyRoutine.domain.user.controller;

import com.saeparam.HeyRoutine.domain.user.dto.request.BankUserMakeRequestDto;
import com.saeparam.HeyRoutine.domain.user.dto.response.BankAccountResponseDto;
import com.saeparam.HeyRoutine.domain.user.dto.response.BankUserMakeResponseDto;
import com.saeparam.HeyRoutine.domain.user.service.BankAccountService;
import com.saeparam.HeyRoutine.global.infra.http.bank.WebClientBankUtil;
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
    private final WebClientBankUtil webClientBankUtil;



    @PostMapping("/verification")
    @Operation(summary = "계좌 1원보내기 API", description = "계좌에 1원을 입금 합니다")
    public ResponseEntity<?> accountVerification(@RequestHeader("Authorization") String token,@RequestParam("account") String account) {
        String email = jwtTokenProvider.getEmail(token.substring(7));
        String result=bankAccountService.accountVerification(token,account);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(result));
    }
}

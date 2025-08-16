package com.saeparam.HeyRoutine.domain.user.service;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BankAccountService {

    public String accountRegistration(String token, String account) {

        return "계좌등록이 완료되었습니다.";
    }

    public String accountVerification(String token, String account) {

        return "인증";
    }
}

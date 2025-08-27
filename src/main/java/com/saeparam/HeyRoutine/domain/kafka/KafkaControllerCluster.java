package com.saeparam.HeyRoutine.domain.kafka;


import com.saeparam.HeyRoutine.domain.finance.dto.response.TransactionHistoryListResponseDto;
import com.saeparam.HeyRoutine.domain.routine.dto.request.SubRoutineRequestDto;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import com.saeparam.HeyRoutine.domain.user.repository.UserRepository;
import com.saeparam.HeyRoutine.global.error.handler.UserHandler;
import com.saeparam.HeyRoutine.global.infra.http.bank.WebClientBankUtil;
import com.saeparam.HeyRoutine.global.security.jwt.JwtTokenProvider;
import com.saeparam.HeyRoutine.global.web.response.ApiResponse;
import com.saeparam.HeyRoutine.global.web.response.code.status.ErrorStatus;
import com.saeparam.HeyRoutine.global.web.response.code.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class KafkaControllerCluster {

    private final KafkaProducerCluster producer;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final WebClientBankUtil webClientBankUtil;

    @PostMapping("/kafka/produce/cluster")
    public String sendMessage(@RequestBody Ranker3RequestDto message) {
        producer.sendMessage(message);

        return "ok";
    }

    @PostMapping("/kafka/test")
    public ResponseEntity<ApiResponse<Void>> kafkaTest(@RequestHeader("Authorization") String token){
        UUID uuid = jwtTokenProvider.getUserId(token.substring(7));
        User user=userRepository.findById(uuid).orElseThrow(()->new UserHandler(ErrorStatus.USER_NOT_FOUND));
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);

        TransactionHistoryListResponseDto transactionHistoryListResponseDtoList=webClientBankUtil.inquireTransactionHistoryList(user.getUserKey(),user.getBankAccount(),thirtyDaysAgo,today)
                .block();
        System.out.println(transactionHistoryListResponseDtoList.toString());

        return ResponseEntity.ok(ApiResponse.onSuccess(null, SuccessStatus.INSERT_SUCCESS.getMessage()));
    }


}
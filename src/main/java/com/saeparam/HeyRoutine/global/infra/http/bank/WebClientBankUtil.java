package com.saeparam.HeyRoutine.global.infra.http.bank;

import com.saeparam.HeyRoutine.domain.finance.dto.request.AccountTransferRequestDto;
import com.saeparam.HeyRoutine.domain.finance.dto.request.CheckAuthCodeRequestDto;
import com.saeparam.HeyRoutine.domain.finance.dto.request.OpenAccountAuthRequestDto;
import com.saeparam.HeyRoutine.domain.finance.dto.request.TransactionHistoryRequestDto;
import com.saeparam.HeyRoutine.domain.finance.dto.response.AccountTransferResponseDto;
import com.saeparam.HeyRoutine.domain.finance.dto.response.CheckAuthCodeResponseDto;
import com.saeparam.HeyRoutine.domain.finance.dto.response.OpenAccountAuthResponseDto;
import com.saeparam.HeyRoutine.domain.finance.dto.response.TransactionHistoryResponseDto;
import com.saeparam.HeyRoutine.domain.user.dto.request.BankAccountHeaderDto;
import com.saeparam.HeyRoutine.domain.user.dto.request.BankAccountMakeRequestDto;
import com.saeparam.HeyRoutine.domain.user.dto.request.BankUserMakeRequestDto;
import com.saeparam.HeyRoutine.global.config.WebClientConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;


@Component
@RequiredArgsConstructor
public class WebClientBankUtil {

    private final WebClientConfig webClientConfig;

    @Value("${bank.base-url}")
    private String baseUrl;
    @Value("${bank.api-version}")
    private String apiVersion;

    @Value("${bank.api-key}")
    private String apiKey;

    private String institutionCode="00100";
    private String fintechAppNo="001";

    /**
     * 공통 헤더 생성
     */
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HHmmss");
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private BankAccountHeaderDto createHeader(String apiName, String apiServiceCode, String userKey) {
        var now = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).withZoneSameInstant(KST);
        String transmissionDate = now.format(DATE_FMT);
        String transmissionTime = now.format(TIME_FMT);

        // 기관거래고유번호는 "YYYYMMDDHHmmss" + 6자리 랜덤숫자(100000-999999)
        String timestamp = now.format(DATE_TIME_FMT);
        String randomDigits = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        String transactionUniqueNo = timestamp + randomDigits;

        return BankAccountHeaderDto.builder()
                .apiName(apiName)
                .transmissionDate(transmissionDate)
                .transmissionTime(transmissionTime)
                .institutionCode(institutionCode)
                .fintechAppNo(fintechAppNo)
                .apiServiceCode(apiServiceCode)
                .institutionTransactionUniqueNo(transactionUniqueNo)
                .apiKey(apiKey)
                .userKey(userKey)
                .build();
    }

    /**
     *  계좌 생성 요청
     * @param userKey 사용자 고유 키
     * @param accountTypeUniqueNo 계좌 상품 고유 번호
     * @param responseDtoClass 응답받을 DTO 클래스
     * @return Mono<T>
     * @param <T> 응답 DTO의 타입
     */
    public <T> Mono<T> createDemandDepositAccount(String userKey, String accountTypeUniqueNo, Class<T> responseDtoClass) {
        String url = baseUrl + apiVersion + "/edu/demandDeposit/createDemandDepositAccount";

        BankAccountHeaderDto header = createHeader(
                "createDemandDepositAccount",
                "createDemandDepositAccount",
                userKey
        );

        BankAccountMakeRequestDto requestDto = new BankAccountMakeRequestDto(header, accountTypeUniqueNo);

        // 3. POST 요청 전송
        return webClientConfig.webClient().method(HttpMethod.POST)
                .uri(url)
                .bodyValue(requestDto) // 위에서 만든 DTO 객체를 body에 담아 전송
                .retrieve()
                // 에러 처리 로직 재사용
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("API Client Error: " + errorBody)))
                )
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("API Server Error: " + errorBody)))
                )
                .bodyToMono(responseDtoClass);
    }

    /**
     * 1원 송금 요청
     */
    public Mono<OpenAccountAuthResponseDto> openAccountAuth(String userKey, String accountNo, String authText) {
        String url = baseUrl + apiVersion + "/edu/accountAuth/openAccountAuth";
        BankAccountHeaderDto header = createHeader("openAccountAuth", "openAccountAuth", userKey);
        OpenAccountAuthRequestDto requestDto = new OpenAccountAuthRequestDto(header, accountNo, authText);
        return webClientConfig.webClient().method(HttpMethod.POST)
                .uri(url)
                .bodyValue(requestDto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("API Error: " + errorBody))))
                .bodyToMono(OpenAccountAuthResponseDto.class);
    }

    /**
     * 계좌 거래내역 단건 조회
     */
    public Mono<TransactionHistoryResponseDto> inquireTransactionHistory(String userKey, String accountNo, String transactionUniqueNo) {
        String url = baseUrl + apiVersion + "/edu/demandDeposit/inquireTransactionHistory";
        BankAccountHeaderDto header = createHeader("inquireTransactionHistory", "inquireTransactionHistory", userKey);
        TransactionHistoryRequestDto requestDto = new TransactionHistoryRequestDto(header, accountNo, transactionUniqueNo);
        return webClientConfig.webClient().method(HttpMethod.POST)
                .uri(url)
                .bodyValue(requestDto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("API Error: " + errorBody))))
                .bodyToMono(TransactionHistoryResponseDto.class);
    }

    /**
     * 1원 송금 검증 요청
     */
    public Mono<CheckAuthCodeResponseDto> checkAuthCode(String userKey, String accountNo, String authText, String authCode) {
        String url = baseUrl + apiVersion + "/edu/accountAuth/checkAuthCode";
        BankAccountHeaderDto header = createHeader("checkAuthCode", "checkAuthCode", userKey);
        CheckAuthCodeRequestDto requestDto = new CheckAuthCodeRequestDto(header, accountNo, authText, authCode);
        return webClientConfig.webClient().method(HttpMethod.POST)
                .uri(url)
                .bodyValue(requestDto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("API Error: " + errorBody))))
                .bodyToMono(CheckAuthCodeResponseDto.class);
    }

    /**
     * 은행 계정생성
     * 이메일값만 넣으면 결과값 response
     */
    public <T, V> Mono<T> makeUserAccount(String email, V requestDto, Class<T> responseDtoClass) {
        String url=baseUrl+apiVersion+"/member";
        BankUserMakeRequestDto bankUserMakeRequestDto = BankUserMakeRequestDto.builder()
                .userId(email)
                .apiKey(apiKey)
                .build();
        return webClientConfig.webClient().method(HttpMethod.POST)
                .uri(url)
                .bodyValue(bankUserMakeRequestDto)

                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("4xx Error: " + errorBody)))
                )
                // 5xx 에러 처리
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("5xx Error: " + errorBody)))
                )
                .bodyToMono(responseDtoClass);
//                .block();
    }

    /**
     * 계좌 입금 요청
     */
    public Mono<AccountTransferResponseDto> deposit(String userKey, String accountNo, long amount, String summary) {
        String url = baseUrl + apiVersion + "/edu/demandDeposit/updateDemandDepositAccountDeposit";
        BankAccountHeaderDto header = createHeader("updateDemandDepositAccountDeposit", "updateDemandDepositAccountDeposit", userKey);
        AccountTransferRequestDto requestDto = AccountTransferRequestDto.builder()
                .header(header)
                .accountNo(accountNo)
                .transactionBalance(String.valueOf(amount))
                .transactionSummary(summary)
                .build();

        return webClientConfig.webClient().method(HttpMethod.POST)
                .uri(url)
                .bodyValue(requestDto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("API Error: " + errorBody))))
                .bodyToMono(AccountTransferResponseDto.class);
    }

    /**
     * 계좌 출금 요청
     */
    public Mono<AccountTransferResponseDto> withdraw(String userKey, String accountNo, long amount, String summary) {
        String url = baseUrl + apiVersion + "/edu/demandDeposit/updateDemandDepositAccountWithdrawal";
        BankAccountHeaderDto header = createHeader("updateDemandDepositAccountWithdrawal", "updateDemandDepositAccountWithdrawal", userKey);
        AccountTransferRequestDto requestDto = AccountTransferRequestDto.builder()
                .header(header)
                .accountNo(accountNo)
                .transactionBalance(String.valueOf(amount))
                .transactionSummary(summary)
                .build();

        return webClientConfig.webClient().method(HttpMethod.POST)
                .uri(url)
                .bodyValue(requestDto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("API Error: " + errorBody))))
                .bodyToMono(AccountTransferResponseDto.class);
    }


    public <T> Mono<T> get(String url, Class<T> responseDtoClass) {
        return webClientConfig.webClient().method(HttpMethod.GET)
                .uri(url)
                .retrieve()
//                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new UserHandler(ErrorStatus.AI_CLIENT_ERROR)))
//                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new UserHandler(ErrorStatus.AI_SERVER_ERROR)))
                .bodyToMono(responseDtoClass);
//                .block();
    }


    public <T, V> Mono<T> post(String url, V requestDto, Class<T> responseDtoClass) {
        return webClientConfig.webClient().method(HttpMethod.POST)
                .uri(url)
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(responseDtoClass);
//                .block();
    }
}
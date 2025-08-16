package com.saeparam.HeyRoutine.global.infra.http.bank;

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
     *  계좌 생성 요청
     * @param userKey 사용자 고유 키
     * @param accountTypeUniqueNo 계좌 상품 고유 번호
     * @param responseDtoClass 응답받을 DTO 클래스
     * @return Mono<T>
     * @param <T> 응답 DTO의 타입
     */
    public <T> Mono<T> createDemandDepositAccount(String userKey, String accountTypeUniqueNo, Class<T> responseDtoClass) {
        String url=baseUrl+apiVersion+"/edu/demandDeposit/createDemandDepositAccount";
        // 1. 현재 시간을 한국 표준시(KST) 기준으로 가져옵니다.
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        // 2. 날짜와 시간 문자열을 생성합니다.
        String transmissionDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String transmissionTime = now.format(DateTimeFormatter.ofPattern("HHmmss"));

        // 3. 고유번호의 앞 14자리를 생성합니다.
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        // 4. 고유번호의 뒷 6자리를 순수한 랜덤 숫자로 생성합니다.
        String randomDigits = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));

        // 5. 모든 부분을 조합하여 최종 거래 고유번호를 생성합니다.
        String transactionUniqueNo = timestamp + randomDigits;


        // 1. Header DTO 객체 생성
        BankAccountHeaderDto header = BankAccountHeaderDto.builder()
                .apiName("createDemandDepositAccount")
                .transmissionDate(transmissionDate) // 일관된 시간 값 사용
                .transmissionTime(transmissionTime)
                .institutionCode(institutionCode) // 설정 파일에서 주입받은 값 사용
                .fintechAppNo(fintechAppNo)       // 설정 파일에서 주입받은 값 사용
                .apiServiceCode("createDemandDepositAccount")
                // 거래 고유번호는 매번 유니크한 값으로 생성
                .institutionTransactionUniqueNo(transactionUniqueNo)
                .apiKey(apiKey)
                .userKey(userKey) // 파라미터로 받은 사용자 키 사용
                .build();

        // 2. 최상위 요청 DTO 객체 생성
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
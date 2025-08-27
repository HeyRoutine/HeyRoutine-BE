package com.saeparam.HeyRoutine.global.infra.http.ai;

import com.saeparam.HeyRoutine.domain.analysis.dto.request.AnalysisMyConsumptionRequestDto;
import com.saeparam.HeyRoutine.domain.analysis.dto.request.DailyModelRequestDto;
import com.saeparam.HeyRoutine.domain.analysis.dto.request.GeminiReqDto;
import com.saeparam.HeyRoutine.domain.analysis.dto.response.AnalysisMyConsumptionResponseDto;
import com.saeparam.HeyRoutine.domain.analysis.dto.response.DailyModelResponseDto;
import com.saeparam.HeyRoutine.domain.analysis.dto.response.GeminiResDto;
import com.saeparam.HeyRoutine.global.config.WebClientConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Gemini 등 LLM 호출을 위한 WebClient 유틸
 */
@Component
@RequiredArgsConstructor
public class WebClientAiUtil {

    private final WebClientConfig webClientConfig;

    @Value("${ai.base-url:https://generativelanguage.googleapis.com}")
    private String baseUrl;

    @Value("${ai.api-key:${ai.api-key}}")
    private String apiKey;

    @Value("${ai.model:gemini-2.0-flash}")
    private String model;

    private String aiUrl="http://127.0.0.1";

    public AnalysisMyConsumptionResponseDto analysisMyConsumption(AnalysisMyConsumptionRequestDto requestDto) {
        String url = aiUrl+":8001"+"/predict";
        System.out.println(url);
        return webClientConfig.webClient().method(HttpMethod.POST)
                .uri(url)
                .bodyValue(requestDto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("AI Error: " + errorBody))))
                .bodyToMono(AnalysisMyConsumptionResponseDto.class)
                .block();
    }

    public DailyModelResponseDto getDailyModel(DailyModelRequestDto requestDto) {
        String url = aiUrl+":8000"+"/recommend";
        return webClientConfig.webClient().method(HttpMethod.POST)
                .uri(url)
                .bodyValue(requestDto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("AI Error: " + errorBody))))
                .bodyToMono(DailyModelResponseDto.class)
                .block();
    }


    public Mono<GeminiResDto> requestWeeklySpendingAnalysis(GeminiReqDto requestDto) {
        String url = baseUrl + "/v1beta/models/" + model + ":generateContent?key=" + apiKey;
        return webClientConfig.webClient().method(HttpMethod.POST)
                .uri(url)
                .bodyValue(requestDto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("AI Error: " + errorBody))))
                .bodyToMono(GeminiResDto.class);
    }
}
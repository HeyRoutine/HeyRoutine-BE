package com.saeparam.HeyRoutine.domain.analysis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saeparam.HeyRoutine.domain.analysis.dto.CategorySpendingDto;
import com.saeparam.HeyRoutine.domain.analysis.dto.TransactionDto;
import com.saeparam.HeyRoutine.domain.analysis.dto.request.AnalysisMyConsumptionRequestDto;
import com.saeparam.HeyRoutine.domain.analysis.dto.request.DailyModelRequestDto;
import com.saeparam.HeyRoutine.domain.analysis.dto.request.GeminiReqDto;
import com.saeparam.HeyRoutine.domain.analysis.dto.request.ProductRecommendRequestDto;
import com.saeparam.HeyRoutine.domain.analysis.dto.response.*;
import com.saeparam.HeyRoutine.domain.finance.dto.response.TransactionHistoryListResponseDto;
import com.saeparam.HeyRoutine.domain.finance.service.FinanceService;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import com.saeparam.HeyRoutine.domain.user.repository.UserRepository;
import com.saeparam.HeyRoutine.global.error.handler.TokenHandler;
import com.saeparam.HeyRoutine.global.error.handler.UserHandler;
import com.saeparam.HeyRoutine.global.infra.http.ai.WebClientAiUtil;
import com.saeparam.HeyRoutine.global.infra.http.bank.WebClientBankUtil;
import com.saeparam.HeyRoutine.global.web.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 소비 패턴 분석 서비스를 담당
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpendingAnalysisService {

    private final FinanceService financeService;
    private final UserRepository userRepository;
    private final WebClientAiUtil webClientAiUtil;
    private final ObjectMapper objectMapper;
    private final WebClientBankUtil webClientBankUtil;




    /**
     * 이번 주 소비 패턴을 분석하여 반환한다.
     */
    public List<String> getWeeklySpendingAnalysis(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);

        TransactionHistoryListResponseDto history = financeService.getTransactionHistoryList(user.getId(), startOfWeek, today);
        if (history == null) {
            return Collections.emptyList();
        }

        String json;
        try {
            json = objectMapper.writeValueAsString(history);
        } catch (JsonProcessingException e) {
            throw new TokenHandler(ErrorStatus.AI_SERVICE_ERROR);
        }

        String prompt = "다음 JSON 데이터를 분석하여 사용자의 주간 소비 패턴을 파악하고, 최대 3개의 핵심 문장으로 분석 결과를 제공하세요. 각 문장은 50자 이내여야 합니다. 소비 분석, 팁, 예상 지출액 등을 포함해 주세요.\n\nJSON 데이터: "
                + json
                + "\n\n출력 형식: ```json\n{\n  \"analysis\": [\n    \"분석 결과 1\",\n    \"분석 결과 2\",\n    \"분석 결과 3\"\n  ]\n}\n```";

        GeminiReqDto request = new GeminiReqDto(prompt);

        GeminiResDto aiRes = webClientAiUtil.requestWeeklySpendingAnalysis(request).block();
        if (aiRes == null || aiRes.getCandidates() == null || aiRes.getCandidates().isEmpty()) {
            return Collections.emptyList();
        }

        String aiText = aiRes.getCandidates().get(0).getContent().getParts().get(0).getText();
        aiText = aiText.replace("```json", "").replace("```", "").trim();
        WeeklySpendingAnalysisAiResponseDto response;
        try {
            response = objectMapper.readValue(aiText, WeeklySpendingAnalysisAiResponseDto.class);
        } catch (JsonProcessingException e) {
            throw new TokenHandler(ErrorStatus.AI_RESPONSE_ERROR);
        }

        if (response.getAnalysis() == null) {
            return Collections.emptyList();
        }
        return response.getAnalysis();
    }

    public DailyModelResponseDto getDailyRoutineRecommend(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        DailyModelResponseDto dailyModelResponseDto=webClientAiUtil.getDailyModel(DailyModelRequestDto.builder()
                .user_id(user.getId().toString())
                .top_k("10")
                .exclude_already_planned(false)
                .allow_owned(true)
                .build());

        return dailyModelResponseDto;

    }

    public ConsumptionAnalysisResponseDto analysisMyConsumptionRecommend(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        LocalDate today=LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);
        TransactionHistoryListResponseDto consumptionList=webClientBankUtil.inquireTransactionHistoryList(user.getUserKey(),user.getBankAccount(),thirtyDaysAgo,today).block();

        List<TransactionHistoryListResponseDto.History> spendingHistory = consumptionList.getRec().getList().stream()
                .filter(history -> "출금".equals(history.getTransactionTypeName()))
                .toList();

        long myTotalSpending = spendingHistory.stream()
                .mapToLong(history -> Long.parseLong(history.getTransactionBalance().replace(",", "")))
                .sum();

        List<String> transactionSummaries = spendingHistory.stream()
                .map(TransactionHistoryListResponseDto.History::getTransactionSummary)
                .toList();
        AnalysisMyConsumptionRequestDto analysisMyConsumptionRequestDto=AnalysisMyConsumptionRequestDto.builder()
                .texts(transactionSummaries).build();
        AnalysisMyConsumptionResponseDto analysisMyConsumptionResponseDto= webClientAiUtil.analysisMyConsumption(analysisMyConsumptionRequestDto);
        List<String> categories = analysisMyConsumptionResponseDto.getLabels();

//        System.out.println(analysisMyConsumptionRequestDto.getTexts());
//        System.out.println(analysisMyConsumptionResponseDto.getLabels());
//        System.out.println(myTotalSpending);

        if (spendingHistory.size() != categories.size()) {
            throw new IllegalStateException("AI 서버의 카테고리 분석 결과와 거래 내역 수가 일치하지 않습니다.");
        }

        // --- 3. 카테고리별로 지출 금액 합산 ---
        // spendingHistory의 각 항목과 categories의 각 항목을 인덱스(i)를 기준으로 매칭하여
        // 카테고리(categories.get(i))를 Key로, 지출액(spendingHistory.get(i))을 Value로 하여 그룹핑하고 합산합니다.
        Map<String, Long> categorySpendingMap = IntStream.range(0, spendingHistory.size())
                .boxed()
                .collect(Collectors.groupingBy(
                        categories::get, // AI가 분류해준 카테고리로 그룹핑
                        Collectors.summingLong(i -> Long.parseLong(spendingHistory.get(i).getTransactionBalance().replace(",", "")))
                ));

        final long averageFor20s = 150000L; // 20대 평균 지출 (고정값)

        List<CategorySpendingDto> categorySpendings = categorySpendingMap.entrySet().stream()
                .map(entry -> {
                    long amount = entry.getValue();
                    // 전체 지출액 대비 현재 카테고리의 지출 비율 계산
                    double percentage = (myTotalSpending == 0) ? 0 : ((double) amount / myTotalSpending) * 100.0;
                    return CategorySpendingDto.builder()
                            .categoryName(entry.getKey())
                            .amount(amount)
                            .percentage(Math.round(percentage * 10.0) / 10.0) // 소수점 첫째 자리까지 반올림
                            .build();
                })
                .sorted(Comparator.comparingLong(CategorySpendingDto::getAmount).reversed()) // 지출액이 큰 순서로 정렬
                .collect(Collectors.toList());

        // 평균 대비 내 지출이 몇 퍼센트 높은지/낮은지 계산
        int comparisonPercentage = (int) Math.round(((double) myTotalSpending / averageFor20s - 1) * 100);

        return ConsumptionAnalysisResponseDto.builder()
                .averageSpendingFor20s(averageFor20s)
                .myTotalSpending(myTotalSpending)
                .comparisonPercentage(comparisonPercentage)
                .categorySpendings(categorySpendings)
                .build();
    }

    public Object recommendProduct(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        LocalDate today=LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);
        TransactionHistoryListResponseDto consumptionList=webClientBankUtil.inquireTransactionHistoryList(user.getUserKey(),user.getBankAccount(),thirtyDaysAgo,today).block();
        // 2. AI 서버에 보낼 요청 DTO 생성
        // 거래내역을 TransactionDto 리스트로 변환
        List<TransactionDto> transactionDtos = (consumptionList != null && consumptionList.getRec() != null)
                ? consumptionList.getRec().getList().stream()
                .map(history -> TransactionDto.from(history, userId.toString()))
                .collect(Collectors.toList())
                : Collections.emptyList();

        // 최종 요청 DTO 생성
        ProductRecommendRequestDto requestDto = ProductRecommendRequestDto.builder()
                .userId(userId.toString())
                .transactions(transactionDtos)
                .topK(10) // 요청 JSON 예시에 따라 10으로 고정
                .build();

        // 3. WebClient를 통해 AI 서버에 추천 요청 및 결과 반환
        return webClientAiUtil.recommendProduct(requestDto);
    }
}

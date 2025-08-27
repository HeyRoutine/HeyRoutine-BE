package com.saeparam.HeyRoutine.domain.analysis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saeparam.HeyRoutine.domain.analysis.dto.request.AnalysisMyConsumptionRequestDto;
import com.saeparam.HeyRoutine.domain.analysis.dto.request.DailyModelRequestDto;
import com.saeparam.HeyRoutine.domain.analysis.dto.request.GeminiReqDto;
import com.saeparam.HeyRoutine.domain.analysis.dto.response.AnalysisMyConsumptionResponseDto;
import com.saeparam.HeyRoutine.domain.analysis.dto.response.DailyModelResponseDto;
import com.saeparam.HeyRoutine.domain.analysis.dto.response.GeminiResDto;
import com.saeparam.HeyRoutine.domain.analysis.dto.response.WeeklySpendingAnalysisAiResponseDto;
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

    public Object analysisMyConsumptionRecommend(UUID userId) {
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
        AnalysisMyConsumptionResponseDto analysisMyConsumptionResponseDto= webClientAiUtil.analysisMyConsumption(AnalysisMyConsumptionRequestDto.builder()
                .texts(transactionSummaries).build());
        System.out.println(analysisMyConsumptionResponseDto.getItems());
        // 여기부터 다시수정 AI서버 에러터짐 십ㅂㅂㅂㅂㅂㅂㅂㅂㅂㅂㅂㅂㅂㅂㅂㅂㅂㅂㅂ발ㄹㄹㄹㄹㄹㄹㄹㄹㄹ
        return null;

    }
}
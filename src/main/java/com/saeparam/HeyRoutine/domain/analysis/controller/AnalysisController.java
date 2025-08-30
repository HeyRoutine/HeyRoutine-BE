package com.saeparam.HeyRoutine.domain.analysis.controller;


import com.saeparam.HeyRoutine.domain.analysis.dto.response.MaxStreakResponseDto;
import com.saeparam.HeyRoutine.domain.analysis.dto.response.WeeklyPointResponseDto;
import com.saeparam.HeyRoutine.domain.analysis.dto.response.WeeklySummaryDto;
import com.saeparam.HeyRoutine.domain.analysis.service.AnalysisPointService;
import com.saeparam.HeyRoutine.domain.analysis.service.AnalysisService;
import com.saeparam.HeyRoutine.domain.analysis.service.SpendingAnalysisService;
import com.saeparam.HeyRoutine.domain.routine.enums.Category;
import com.saeparam.HeyRoutine.domain.routine.enums.RoutineType;
import com.saeparam.HeyRoutine.global.security.jwt.JwtTokenProvider;
import com.saeparam.HeyRoutine.global.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;
    private final JwtTokenProvider jwtTokenProvider;
    private final SpendingAnalysisService spendingAnalysisService;
    private final AnalysisPointService analysisPointService;

    @GetMapping("/weekly-summary")
    @Operation(summary = "주간 요약 데이터 조회 API", description = "선택된 기간 동안의 루틴별 수행 여부를 조회합니다.")
    public ResponseEntity<?> getWeeklySummary(
            @RequestHeader("Authorization") String token, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate, @RequestParam RoutineType routineType
            ) {
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        List<WeeklySummaryDto> result = analysisService.getWeeklySummaries(userId, startDate, endDate,routineType);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }

    @GetMapping("/max-streak")
    @Operation(summary = "최대 연속 달성일 조회 API", description = "개인 및 그룹 루틴을 포함한 최대 연속 달성일을 조회합니다.")
    public ResponseEntity<?> getMaxStreak(@RequestHeader("Authorization") String token) {
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        MaxStreakResponseDto result = analysisService.calculateMaxStreak(userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }

    @GetMapping("/weekly")
    @Operation(summary = "이번 주 소비패턴 분석 조회 API", description = "이번 주 소비내역을 AI로 분석합니다.")
    public ResponseEntity<?> getWeeklySpendingAnalysis(@RequestHeader("Authorization") String token) {
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        List<String> result = spendingAnalysisService.getWeeklySpendingAnalysis(userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }

    @GetMapping("/daily")
    @Operation(summary = "생활,소비루틴 추천 조회 API", description = "생활,소비루틴 추천을 AI가 추천해줍니다.")
    public ResponseEntity<?> dailyRoutineRecommend(@RequestHeader("Authorization") String token) {
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));

        return ResponseEntity.ok(ApiResponse.onSuccess(spendingAnalysisService.getDailyRoutineRecommend(userId)));
    }

    @GetMapping("/category")
    @Operation(summary = "소비분석해서 패턴확인  API", description = "소비를 분석해서 해당유저의 소비패턴 분석")
    public ResponseEntity<?> categoryRoutineRecommend(@RequestHeader("Authorization") String token) {
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));

        return ResponseEntity.ok(ApiResponse.onSuccess(spendingAnalysisService.analysisMyConsumptionRecommend(userId)));
    }


    @GetMapping("/recommend-product")
    @Operation(summary = "금융상품 추천  API", description = "해당유저의 소비패턴을 분석하여 금융상품 추천")
    public ResponseEntity<?> recommendProduct(@RequestHeader("Authorization") String token) {
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));

        return ResponseEntity.ok(ApiResponse.onSuccess(spendingAnalysisService.recommendProduct(userId)));
    }


    @PostMapping("/weekly-point")
    @Operation(summary = "연속 7일 달성 포인트 지급 API", description = "연속 7일 분석 달성 시 100p 지급")
    public ResponseEntity<?> giveWeeklyPoint(@RequestHeader("Authorization") String token) {
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        WeeklyPointResponseDto result = analysisPointService.giveWeeklyPoint(userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }
}
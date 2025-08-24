package com.saeparam.HeyRoutine.domain.analysis.controller;


import com.saeparam.HeyRoutine.domain.analysis.dto.response.MaxStreakResponseDto;
import com.saeparam.HeyRoutine.domain.analysis.dto.response.WeeklySummaryDto;
import com.saeparam.HeyRoutine.domain.analysis.service.AnalysisService;
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
}
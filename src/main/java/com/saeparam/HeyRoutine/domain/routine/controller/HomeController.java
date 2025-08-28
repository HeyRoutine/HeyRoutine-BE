package com.saeparam.HeyRoutine.domain.routine.controller;

import com.saeparam.HeyRoutine.domain.routine.dto.response.GroupRoutineResponseDto;
import com.saeparam.HeyRoutine.domain.routine.service.GroupRoutineService;
import com.saeparam.HeyRoutine.global.security.jwt.JwtTokenProvider;
import com.saeparam.HeyRoutine.global.web.response.ApiResponse;
import com.saeparam.HeyRoutine.global.web.response.PaginatedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * <h2>HomeController</h2>
 * <p>홈 화면 관련 API를 제공하는 컨트롤러입니다.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/home")
@Tag(name = "Home", description = "홈 화면 관련 API")
public class HomeController {

    private final GroupRoutineService groupRoutineService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 내가 가입한 단체 루틴 목록을 조회합니다.
     *
     * @param token    인증 토큰
     * @param pageable 페이지 정보 (기본 10개, 생성일 기준 내림차순)
     * @return 단체 루틴 목록
     */
    @GetMapping("/groups")
    @Operation(summary = "내 단체루틴 조회 API", description = "사용자가 가입한 단체루틴 목록을 최신순으로 조회합니다.")
    public ResponseEntity<ApiResponse<PaginatedResponse<GroupRoutineResponseDto.MyGroupRoutineInfo>>> getMyGroupRoutines(
            @RequestHeader("Authorization") String token,
            @PageableDefault(page = 0, size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        UUID uuid = jwtTokenProvider.getUserId(token.substring(7));
        PaginatedResponse<GroupRoutineResponseDto.MyGroupRoutineInfo> response =
                groupRoutineService.getMyGroupRoutines(uuid, pageable);
        if (response.items().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.noContent());
        }
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }
}
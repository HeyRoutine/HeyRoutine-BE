package com.saeparam.HeyRoutine.domain.routine.controller;

import com.saeparam.HeyRoutine.domain.routine.dto.response.CommonResponseDto;
import com.saeparam.HeyRoutine.domain.routine.enums.Category;
import com.saeparam.HeyRoutine.domain.routine.service.RoutineCommonService;
import com.saeparam.HeyRoutine.global.error.handler.RoutineHandler;
import com.saeparam.HeyRoutine.global.web.response.ApiResponse;
import com.saeparam.HeyRoutine.global.web.response.PaginatedResponse;
import com.saeparam.HeyRoutine.global.web.response.code.status.ErrorStatus;
import com.saeparam.HeyRoutine.global.web.response.code.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 루틴 공통 API 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/routines")
@Tag(name = "Routine-Common", description = "루틴 공통 API")
public class RoutineCommonController {

    private final RoutineCommonService routineCommonService;

    @GetMapping("/templates")
    @Operation(summary = "루틴 템플릿 목록 조회 API", description = "카테고리에 따른 루틴 템플릿을 페이지네이션하여 조회합니다.")
    public ResponseEntity<ApiResponse<PaginatedResponse<CommonResponseDto.TemplateInfo>>> getRoutineTemplates(
            @RequestParam(required = false) String category,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        Category categoryEnum = Category.from(category);
        if (category != null && categoryEnum == null) {
            throw new RoutineHandler(ErrorStatus.INVALID_CATEGORY);
        }

        PaginatedResponse<CommonResponseDto.TemplateInfo> response =
                routineCommonService.getRoutineTemplates(categoryEnum, pageable);

        if (response.items().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.of(SuccessStatus.NO_CONTENT, response));
        }
        return ResponseEntity.ok(ApiResponse.onSuccess(response, "템플릿 목록 조회 성공"));
    }
}
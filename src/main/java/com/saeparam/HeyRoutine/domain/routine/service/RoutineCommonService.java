package com.saeparam.HeyRoutine.domain.routine.service;

import com.saeparam.HeyRoutine.domain.routine.dto.response.CommonResponseDto;
import com.saeparam.HeyRoutine.domain.routine.enums.Category;
import com.saeparam.HeyRoutine.global.web.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;

/**
 * <h2>RoutineCommonService</h2>
 * <p>루틴 관련 공통 API 비즈니스 로직을 정의하는 서비스 인터페이스입니다.</p>
 *
 * </p>
 * <pre>
 * ┌──────────────┐      DTOs      ┌─────────────┐      Entities      ┌──────────────┐
 * │  Controller  │◄──────────────►│   Service   │◄──────────────────►│  Repository  │
 * └──────────────┘                └─────────────┘                    └──────────────┘
 * </pre>
 */
public interface RoutineCommonService {

    /**
     * 카테고리에 따라 루틴 템플릿 목록을 조회합니다.
     *
     * @param category 필터링할 카테고리 (null일 경우 전체 조회)
     * @param pageable 페이지 정보
     * @return 페이징 처리된 템플릿 정보
     */
    PaginatedResponse<CommonResponseDto.TemplateInfo> getRoutineTemplates(Category category, Pageable pageable);
}
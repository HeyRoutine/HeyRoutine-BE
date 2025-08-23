package com.saeparam.HeyRoutine.domain.routine.service;


import com.saeparam.HeyRoutine.domain.routine.dto.response.CommonResponseDto;
import com.saeparam.HeyRoutine.domain.routine.entity.Template;
import com.saeparam.HeyRoutine.domain.routine.enums.Category;
import com.saeparam.HeyRoutine.domain.routine.repository.TemplateRepository;
import com.saeparam.HeyRoutine.global.web.response.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <h2>RoutineCommonServiceImpl</h2>
 * <p>루틴 관련 공통 API 비즈니스 로직을 구현한 서비스 클래스입니다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoutineCommonServiceImpl implements RoutineCommonService {

    private final TemplateRepository templateRepository;

    @Override
    public PaginatedResponse<CommonResponseDto.TemplateInfo> getRoutineTemplates(Category category, Pageable pageable) {
        Page<Template> page = (category == null)
                ? templateRepository.findAll(pageable)
                : templateRepository.findByCategory(category, pageable);

        return PaginatedResponse.of(page, template -> CommonResponseDto.TemplateInfo.builder()
                .templateId(template.getId())
                .emojiId(template.getEmoji().getId())
                .name(template.getName())
                .content(template.getContent())
                .build());
    }
}
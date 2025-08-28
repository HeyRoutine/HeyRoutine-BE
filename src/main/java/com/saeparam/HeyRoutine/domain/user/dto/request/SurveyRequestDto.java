package com.saeparam.HeyRoutine.domain.user.dto.request;

import lombok.*;

import java.util.List;

// JSON의 Header 객체와 매핑될 DTO
@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyRequestDto {
    private List<Boolean> surveyList;
}
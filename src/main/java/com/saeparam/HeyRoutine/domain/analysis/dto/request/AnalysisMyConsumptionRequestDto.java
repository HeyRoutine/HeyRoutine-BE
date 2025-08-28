package com.saeparam.HeyRoutine.domain.analysis.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisMyConsumptionRequestDto {

    private List<String> texts;
}
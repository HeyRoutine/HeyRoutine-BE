package com.saeparam.HeyRoutine.domain.analysis.dto.response;

import com.saeparam.HeyRoutine.domain.routine.enums.DayType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WeeklySummaryDto {
    private String routineTitle;
    private Map<DayType, Boolean> dailyStatus;


}
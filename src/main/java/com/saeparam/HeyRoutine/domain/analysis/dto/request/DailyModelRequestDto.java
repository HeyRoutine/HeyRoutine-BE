package com.saeparam.HeyRoutine.domain.analysis.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyModelRequestDto {
    private String user_id;
    private String top_k;
    private boolean exclude_already_planned;
    private boolean allow_owned;
}
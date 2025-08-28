package com.saeparam.HeyRoutine.domain.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ResultDto {
    private String bankName;
    private String accountTypeName;
    private String accountDscription;
    private int subscriptionPeriod;
    private double interesRate;
    private double score;
    private int rank;
}
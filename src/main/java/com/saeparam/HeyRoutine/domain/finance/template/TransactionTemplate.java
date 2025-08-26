package com.saeparam.HeyRoutine.domain.finance.template;

/**
 * 거래 내역 템플릿을 나타내는 간단한 DTO.
 */
public record TransactionTemplate(String summary, long amount) {}
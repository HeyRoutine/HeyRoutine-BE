package com.saeparam.HeyRoutine.domain.finance.template;

import java.util.List;

/**
 * 더미 입금 템플릿 모음.
 */
public final class DepositTemplates {
    private DepositTemplates() {}

    public static final List<TransactionTemplate> TEMPLATES = List.of(
            new TransactionTemplate("월급", 700_000L),
            new TransactionTemplate("알바비", 400_000L),
            new TransactionTemplate("용돈", 300_000L),
            new TransactionTemplate("장학금", 500_000L),
            new TransactionTemplate("정부지원금", 600_000L),
            new TransactionTemplate("보너스", 550_000L),
            new TransactionTemplate("이자", 450_000L),
            new TransactionTemplate("환급금", 650_000L),
            new TransactionTemplate("세금환급", 750_000L),
            new TransactionTemplate("기타소득", 350_000L)
    );
}
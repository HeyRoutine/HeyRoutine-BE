package com.saeparam.HeyRoutine.domain.finance.template;

import java.util.List;

/**
 * 더미 출금 템플릿 모음.
 */
public final class ExpenseTemplates {
    private ExpenseTemplates() {}

    public static final List<TransactionTemplate> TEMPLATES = List.of(
            new TransactionTemplate("CU", 10_000L),
            new TransactionTemplate("GS25", 15_000L),
            new TransactionTemplate("이마트", 40_000L),
            new TransactionTemplate("스타벅스", 6_000L),
            new TransactionTemplate("맥도날드", 8_000L),
            new TransactionTemplate("버거킹", 9_000L),
            new TransactionTemplate("롯데리아", 7_000L),
            new TransactionTemplate("카페베네", 5_000L),
            new TransactionTemplate("교보문고", 20_000L),
            new TransactionTemplate("CGV", 12_000L),
            new TransactionTemplate("배달의민족", 15_000L),
            new TransactionTemplate("요기요", 13_000L),
            new TransactionTemplate("쿠팡", 25_000L),
            new TransactionTemplate("티머니", 10_000L),
            new TransactionTemplate("택시비", 11_000L),
            new TransactionTemplate("세탁소", 8_000L),
            new TransactionTemplate("미용실", 30_000L),
            new TransactionTemplate("넷플릭스", 14_500L),
            new TransactionTemplate("멜론", 10_000L),
            new TransactionTemplate("아마존", 20_000L),
            new TransactionTemplate("올리브영", 18_000L),
            new TransactionTemplate("다이소", 5_000L),
            new TransactionTemplate("한솥", 7_000L),
            new TransactionTemplate("배스킨라빈스", 9_000L),
            new TransactionTemplate("파리바게뜨", 8_000L),
            new TransactionTemplate("버스킹 후원", 5_000L),
            new TransactionTemplate("코인노래방", 6_000L),
            new TransactionTemplate("PC방", 10_000L),
            new TransactionTemplate("학교식당", 4_500L),
            new TransactionTemplate("문구점", 3_000L),
            new TransactionTemplate("롯데시네마", 11_000L),
            new TransactionTemplate("탑텐", 35_000L),
            new TransactionTemplate("ABC마트", 60_000L),
            new TransactionTemplate("네이버페이", 20_000L),
            new TransactionTemplate("카카오T", 15_000L),
            new TransactionTemplate("GS칼텍스", 70_000L),
            new TransactionTemplate("주유소", 80_000L),
            new TransactionTemplate("도서관카페", 4_500L),
            new TransactionTemplate("레스토랑", 45_000L),
            new TransactionTemplate("세븐일레븐", 12_000L),
            new TransactionTemplate("홈플러스", 30_000L),
            new TransactionTemplate("빵집", 6_000L),
            new TransactionTemplate("분식집", 7_000L),
            new TransactionTemplate("국밥집", 8_000L),
            new TransactionTemplate("치킨집", 20_000L),
            new TransactionTemplate("피자가게", 30_000L),
            new TransactionTemplate("스시집", 35_000L),
            new TransactionTemplate("영화관", 13_000L),
            new TransactionTemplate("볼링장", 15_000L),
            new TransactionTemplate("노래방", 12_000L)
    );
}
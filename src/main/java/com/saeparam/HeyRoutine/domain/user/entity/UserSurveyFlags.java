package com.saeparam.HeyRoutine.domain.user.entity;


import com.saeparam.HeyRoutine.domain.routine.enums.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 루틴에 사용되는 이모지 엔티티
 * <p>카테고리별 이모지 조회를 지원하기 위해 {@link Category} 필드를 포함합니다.</p>
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSurveyFlags {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "servey_id", updatable = false, unique = true, nullable = false)
    private long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private boolean q0;
    @Column(nullable = false)
    private boolean q1;
    @Column(nullable = false)
    private boolean q2;
    @Column(nullable = false)
    private boolean q3;
    @Column(nullable = false)
    private boolean q4;
    @Column(nullable = false)
    private boolean q5;
    @Column(nullable = false)
    private boolean q6;
    @Column(nullable = false)
    private boolean q7;
    @Column(nullable = false)
    private boolean q8;
    @Column(nullable = false)
    private boolean q9;
    @Column(nullable = false)
    private boolean q10;
    @Column(nullable = false)
    private boolean q11;
    @Column(nullable = false)
    private boolean q12;
    @Column(nullable = false)
    private boolean q13;
    @Column(nullable = false)
    private boolean q14;
    @Column(nullable = false)
    private boolean q15;
    @Column(nullable = false)
    private boolean q16;
    @Column(nullable = false)
    private boolean q17;
    @Column(nullable = false)
    private boolean q18;
    @Column(nullable = false)
    private boolean q19;
    @Column(nullable = false)
    private boolean q20;
    @Column(nullable = false)
    private boolean q21;
    @Column(nullable = false)
    private boolean q22;
    @Column(nullable = false)
    private boolean q23;
    @Column(nullable = false)
    private boolean q24;
    @Column(nullable = false)
    private boolean q25;
    @Column(nullable = false)
    private boolean q26;
    @Column(nullable = false)
    private boolean q27;
    @Column(nullable = false)
    private boolean q28;
    @Column(nullable = false)
    private boolean q29;
    @Column(nullable = false)
    private boolean q30;
    @Column(nullable = false)
    private boolean q31;
    @Column(nullable = false)
    private boolean q32;

    public void updateFlags(List<Boolean> surveyList) {
        // 리스트 크기 검증은 서비스 계층에서 이미 수행했다고 가정합니다.
        this.q0 = surveyList.get(0);
        this.q1 = surveyList.get(1);
        this.q2 = surveyList.get(2);
        this.q3 = surveyList.get(3);
        this.q4 = surveyList.get(4);
        this.q5 = surveyList.get(5);
        this.q6 = surveyList.get(6);
        this.q7 = surveyList.get(7);
        this.q8 = surveyList.get(8);
        this.q9 = surveyList.get(9);
        this.q10 = surveyList.get(10);
        this.q11 = surveyList.get(11);
        this.q12 = surveyList.get(12);
        this.q13 = surveyList.get(13);
        this.q14 = surveyList.get(14);
        this.q15 = surveyList.get(15);
        this.q16 = surveyList.get(16);
        this.q17 = surveyList.get(17);
        this.q18 = surveyList.get(18);
        this.q19 = surveyList.get(19);
        this.q20 = surveyList.get(20);
        this.q21 = surveyList.get(21);
        this.q22 = surveyList.get(22);
        this.q23 = surveyList.get(23);
        this.q24 = surveyList.get(24);
        this.q25 = surveyList.get(25);
        this.q26 = surveyList.get(26);
        this.q27 = surveyList.get(27);
        this.q28 = surveyList.get(28);
        this.q29 = surveyList.get(29);
        this.q30 = surveyList.get(30);
        this.q31 = surveyList.get(31);
        this.q32 = surveyList.get(32);
    }


}

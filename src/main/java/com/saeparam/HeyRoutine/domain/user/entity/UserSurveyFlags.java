package com.saeparam.HeyRoutine.domain.user.entity;


import com.saeparam.HeyRoutine.domain.routine.enums.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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



}

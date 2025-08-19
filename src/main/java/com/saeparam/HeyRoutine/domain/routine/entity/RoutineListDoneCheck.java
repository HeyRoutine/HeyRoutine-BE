package com.saeparam.HeyRoutine.domain.routine.entity;

import com.saeparam.HeyRoutine.domain.user.entity.User;
import com.saeparam.HeyRoutine.global.common.util.BaseTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutineListDoneCheck extends BaseTime { // BaseTime 클래스를 상속받아 생성/수정 시간을 기록합니다.

    @Column(name = "my_routine_list_done_check_id", updatable = false, unique = true, nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "my_routine_list_id", nullable = false)
    private MyRoutineList myRoutineList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "done_check", nullable = false)
    private boolean doneCheck;
}
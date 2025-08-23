package com.saeparam.HeyRoutine.domain.routine.entity;

import com.saeparam.HeyRoutine.domain.user.entity.User;
import com.saeparam.HeyRoutine.global.common.util.BaseTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupRoutineListDoneCheck extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_routine_list_done_check_id", updatable = false, unique = true, nullable = false)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_routine_list_id", nullable = false)
    private GroupRoutineList groupRoutineList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "done_check", nullable = false)
    private boolean doneCheck;


    // ################# 비즈니스 로직 메서드 #################

    /**
     * 완료 여부를 업데이트합니다.
     *
     * @param doneCheck true: 성공, false: 실패
     */
    public void updateDoneCheck(boolean doneCheck) {
        this.doneCheck = doneCheck;
    }

}

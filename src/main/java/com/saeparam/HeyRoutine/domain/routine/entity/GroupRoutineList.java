package com.saeparam.HeyRoutine.domain.routine.entity;

import com.saeparam.HeyRoutine.domain.user.entity.User;
import com.saeparam.HeyRoutine.global.common.util.BaseTime;
import jakarta.persistence.*;
import lombok.*;

import com.saeparam.HeyRoutine.domain.routine.enums.RoutineType;
import java.time.LocalTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupRoutineList extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_routine_list_id", updatable = false, unique = true, nullable = false)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "routine_type", nullable = false)
    private RoutineType routineType;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "startTime", nullable = false)
    private LocalTime startTime;

    @Column(name = "endTime", nullable = false)
    private LocalTime endTime;

    @Column(name = "userCnt", nullable = false)
    private int userCnt;

    // ################# 비즈니스 로직 메서드 #################

    /**
     * 단체 루틴의 기본 정보를 수정합니다.
     *
     * @param title       수정할 제목
     * @param description 수정할 설명
     * @param routineType 루틴 타입
     * @param startTime   시작 시간
     * @param endTime     종료 시간
     */
    public void update(String title, String description, RoutineType routineType,
                       LocalTime startTime, LocalTime endTime) {
        this.title = title;
        this.description = description;
        this.routineType = routineType;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}

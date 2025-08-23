package com.saeparam.HeyRoutine.domain.routine.entity;


import com.saeparam.HeyRoutine.domain.routine.dto.request.MyRoutineListRequestDto;
import com.saeparam.HeyRoutine.domain.routine.enums.RoutineType;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import com.saeparam.HeyRoutine.global.common.util.BaseTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyRoutineList extends BaseTime {

    @Column(name = "my_routine_list_id", updatable = false, unique = true, nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "startDate", nullable = false)
    private LocalDate startDate;

    @Column(name = "startTime", nullable = false)
    private LocalTime startTime;

    @Column(name = "endTime", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "routine_type", nullable = false)
    private RoutineType routineType;

    @Builder.Default
    @OneToMany(mappedBy = "routineList", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MyRoutineDays> routineDays=new HashSet<>();

    @OneToMany(mappedBy = "routineList", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MyRoutineMiddle> routineMiddles;


    public void update(MyRoutineListRequestDto requestDto) {
        this.title = requestDto.getTitle();
        this.startDate = requestDto.getStartDate();
        this.startTime = requestDto.getStartTime();
        this.endTime = requestDto.getEndTime();
        this.routineType = requestDto.getRoutineType();

        // 기존 요일 정보를 모두 삭제하고, 새로운 요일 정보로 교체합니다.
        this.routineDays.clear();
        if (requestDto.getDayTypes() != null) {
            Set<MyRoutineDays> newRoutineDays = requestDto.getDayTypes().stream()
                    .map(dayType -> MyRoutineDays.builder()
                            .routineList(this)
                            .dayType(dayType)
                            .build())
                    .collect(Collectors.toSet());
            this.routineDays.addAll(newRoutineDays);
        }
    }
//    public void addRoutineDay(MyRoutineDays routineDay) {
//        this.routineDays.add(routineDay);
//        routineDay.setRoutineList(this);
//    }



}

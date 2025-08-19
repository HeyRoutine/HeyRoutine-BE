package com.saeparam.HeyRoutine.domain.routine.entity;


import com.saeparam.HeyRoutine.domain.routine.enums.DayType;
import com.saeparam.HeyRoutine.global.common.util.BaseTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyRoutineDays extends BaseTime {


    @Column(name = "my_routine_days_id", updatable = false, unique = true, nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "my_routine_list_id")
    private MyRoutineList routineList;

    @Column(nullable = false)
    private DayType dayType;




}

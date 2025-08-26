package com.saeparam.HeyRoutine.domain.routine.repository;

import com.saeparam.HeyRoutine.domain.routine.entity.Emoji;
import com.saeparam.HeyRoutine.domain.routine.entity.MyRoutineList;
import com.saeparam.HeyRoutine.domain.routine.entity.MyRoutineMiddle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MyRoutineMiddleRepository extends JpaRepository<MyRoutineMiddle, Long> {
    long countByRoutineList(MyRoutineList routineList);

    List<MyRoutineMiddle> findByRoutineList(MyRoutineList routineList);
}
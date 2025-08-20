package com.saeparam.HeyRoutine.domain.routine.repository;

import com.saeparam.HeyRoutine.domain.routine.entity.MyRoutineDays;
import com.saeparam.HeyRoutine.domain.routine.entity.MyRoutineList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyRoutineDaysRepository extends JpaRepository<MyRoutineDays, Long> {

}
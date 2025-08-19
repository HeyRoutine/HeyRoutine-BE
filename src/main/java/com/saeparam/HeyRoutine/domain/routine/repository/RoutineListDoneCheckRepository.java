package com.saeparam.HeyRoutine.domain.routine.repository;

import com.saeparam.HeyRoutine.domain.routine.entity.MyRoutineList;
import com.saeparam.HeyRoutine.domain.routine.entity.RoutineListDoneCheck;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoutineListDoneCheckRepository extends JpaRepository<RoutineListDoneCheck, Long> {


}
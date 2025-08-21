package com.saeparam.HeyRoutine.domain.routine.repository;

import com.saeparam.HeyRoutine.domain.routine.entity.GroupRoutineList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRoutinDaysRepository extends JpaRepository<GroupRoutineList, Long> {
}

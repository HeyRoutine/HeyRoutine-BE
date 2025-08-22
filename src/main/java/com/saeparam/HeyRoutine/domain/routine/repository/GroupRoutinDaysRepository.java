package com.saeparam.HeyRoutine.domain.routine.repository;


import com.saeparam.HeyRoutine.domain.routine.entity.GroupRoutineDays;
import com.saeparam.HeyRoutine.domain.routine.entity.GroupRoutineList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRoutinDaysRepository extends JpaRepository<GroupRoutineDays, Long> {

    /**
     * 주어진 단체 루틴에 설정된 요일 정보를 조회합니다.
     *
     * @param groupRoutineList 단체 루틴
     * @return 요일 정보 목록
     */
    List<GroupRoutineDays> findByGroupRoutineList(GroupRoutineList groupRoutineList);
}
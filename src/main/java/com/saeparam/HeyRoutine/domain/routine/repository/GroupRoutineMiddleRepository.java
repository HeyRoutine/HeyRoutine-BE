package com.saeparam.HeyRoutine.domain.routine.repository;

import com.saeparam.HeyRoutine.domain.routine.entity.GroupRoutineList;
import com.saeparam.HeyRoutine.domain.routine.entity.GroupRoutineMiddle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRoutineMiddleRepository extends JpaRepository<GroupRoutineMiddle, Long> {
    /**
     * 주어진 단체 루틴에 속한 상세 루틴의 개수를 반환합니다.
     *
     * @param routineList 상세 루틴의 상위 단체 루틴
     * @return 상세 루틴 개수
     */
    long countByRoutineList(GroupRoutineList routineList);

    /**
     * 특정 단체 루틴에 속한 모든 상세 루틴을 삭제합니다.
     *
     * @param routineList 단체 루틴
     */
    void deleteAllByRoutineList(GroupRoutineList routineList);
}

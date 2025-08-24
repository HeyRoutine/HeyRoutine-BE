package com.saeparam.HeyRoutine.domain.routine.repository;

import com.saeparam.HeyRoutine.domain.routine.entity.GroupRoutineList;
import com.saeparam.HeyRoutine.domain.routine.entity.GroupRoutineMiddle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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

    /**
     * 특정 단체 루틴에 속한 모든 상세 루틴 정보를 조회합니다.
     * 루틴 엔티티를 함께 가져오기 위해 fetch join을 사용합니다.
     *
     * @param routineList 단체 루틴
     * @return 상세 루틴 목록
     */
    @Query("SELECT m FROM GroupRoutineMiddle m JOIN FETCH m.routine WHERE m.routineList = :routineList")
    List<GroupRoutineMiddle> findWithRoutineByRoutineList(@Param("routineList") GroupRoutineList routineList);

    /**
     * 주어진 단체 루틴에 속한 모든 상세 루틴을 조회합니다.
     *
     * @param routineList 상세 루틴의 상위 단체 루틴
     * @return 상세 루틴 목록
     */
    List<GroupRoutineMiddle> findByRoutineList(GroupRoutineList routineList);

    /**
     * 특정 단체 루틴에 속한 특정 상세 루틴을 조회합니다.
     *
     * @param routineList 단체 루틴
     * @param routineId   상세 루틴 ID
     * @return 상세 루틴 매핑 정보
     */
    @Query("SELECT m FROM GroupRoutineMiddle m JOIN FETCH m.routine WHERE m.routineList = :routineList AND m.routine.id = :routineId")
    java.util.Optional<GroupRoutineMiddle> findByRoutineListAndRoutineId(@Param("routineList") GroupRoutineList routineList,
                                                                         @Param("routineId") Long routineId);
}

package com.saeparam.HeyRoutine.domain.routine.repository;

import com.saeparam.HeyRoutine.domain.routine.entity.GroupRoutineList;
import com.saeparam.HeyRoutine.domain.routine.entity.GroupRoutineListDoneCheck;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * <h2>GroupRoutineListDoneCheckRepository</h2>
 * <p>
 * 단체 루틴 완료 여부를 기록한 {@link GroupRoutineListDoneCheck} 엔티티에 대한
 * 데이터 접근을 담당하는 Repository 인터페이스입니다. <br>
 * 단체 루틴과 사용자 기준으로 완료 여부를 조회하거나, 완료/미완료 인원 수를
 * 집계하는 기능을 제공합니다.
 * </p>
 */
@Repository
public interface GroupRoutineListDoneCheckRepository extends JpaRepository<GroupRoutineListDoneCheck, Long> {

    /**
     * 특정 단체 루틴과 사용자 기준으로 완료 여부 엔티티를 조회합니다.
     *
     * @param groupRoutineList 단체 루틴
     * @param user             사용자
     * @return {@link Optional} 조회된 완료 여부 엔티티
     */
    Optional<GroupRoutineListDoneCheck> findByGroupRoutineListAndUser(GroupRoutineList groupRoutineList, User user);

    /**
     * 단체 루틴에 대해 특정 완료 여부를 기록한 인원 수를 계산합니다.
     *
     * @param groupRoutineList 단체 루틴
     * @param doneCheck        완료 여부(true: 성공, false: 실패)
     * @return 해당 완료 여부를 기록한 인원 수
     */
    long countByGroupRoutineListAndDoneCheck(GroupRoutineList groupRoutineList, boolean doneCheck);

    /**
     * 단체 루틴과 연관된 모든 완료 여부 기록을 삭제합니다.
     *
     * @param groupRoutineList 단체 루틴
     */
    void deleteAllByGroupRoutineList(GroupRoutineList groupRoutineList);

    List<GroupRoutineListDoneCheck> findByUserAndCreatedDateBetween(User user, LocalDateTime start, LocalDateTime end);
}

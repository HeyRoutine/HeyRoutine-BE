package com.saeparam.HeyRoutine.domain.routine.repository;

import com.saeparam.HeyRoutine.domain.routine.entity.GroupRoutineList;
import com.saeparam.HeyRoutine.domain.routine.entity.UserInRoom;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;


@Repository
public interface UserInRoomRepository extends JpaRepository<UserInRoom, Long> {

    /**
     * 특정 단체 루틴에 참여중인 인원 수를 반환합니다.
     *
     * @param groupRoutineList 단체 루틴
     * @return 참여 인원 수
     */
    long countByGroupRoutineList(GroupRoutineList groupRoutineList);

    /**
     * 사용자가 해당 단체 루틴에 참여중인지 여부를 확인합니다.
     *
     * @param groupRoutineList 단체 루틴
     * @param user             확인할 사용자
     * @return true: 참여중, false: 미참여
     */
    boolean existsByGroupRoutineListAndUser(GroupRoutineList groupRoutineList, User user);

    /**
     * 특정 단체 루틴에 참여중인 모든 사용자 정보를 삭제합니다.
     *
     * @param groupRoutineList 단체 루틴
     */
    void deleteAllByGroupRoutineList(GroupRoutineList groupRoutineList);

    /**
     * 단체 루틴에 속한 모든 참여자 정보를 조회합니다.
     *
     * @param groupRoutineList 단체 루틴
     * @return 참여자 목록
     */
    @Query("SELECT uir FROM UserInRoom uir JOIN FETCH uir.user WHERE uir.groupRoutineList = :groupRoutineList")
    List<UserInRoom> findByGroupRoutineList(@Param("groupRoutineList") GroupRoutineList groupRoutineList);
}

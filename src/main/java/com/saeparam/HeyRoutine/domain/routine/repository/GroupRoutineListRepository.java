package com.saeparam.HeyRoutine.domain.routine.repository;

import com.saeparam.HeyRoutine.domain.routine.entity.GroupRoutineList;
import com.saeparam.HeyRoutine.domain.routine.enums.DayType;
import com.saeparam.HeyRoutine.domain.routine.enums.RoutineType;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRoutineListRepository extends JpaRepository<GroupRoutineList, Long> {
    @Query("SELECT grl FROM GroupRoutineList grl JOIN grl.userInRooms uir WHERE uir.user = :user and grl.routineType=:routineType")
    List<GroupRoutineList> findAllByUserInAndRoutineType(@Param("user") User user, RoutineType routineType);
    @Query("SELECT grl FROM GroupRoutineList grl " +
            "JOIN grl.userInRooms uir " +
            "JOIN grl.groupRoutineDays grd " +
            "WHERE uir.user = :user AND grd.dayType = :day")
    List<GroupRoutineList> findAllByUserAndDay(@Param("user") User user, @Param("day") DayType day);

    @Query("SELECT grl FROM GroupRoutineList grl " +
            "WHERE LOWER(grl.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(grl.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY grl.createdDate DESC")
    Page<GroupRoutineList> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 사용자가 참여중인 단체 루틴 목록을 페이징 조회합니다.
     *
     * @param user     조회할 사용자
     * @param pageable 페이지 정보
     * @return 참여중인 단체 루틴 페이지
     */
    @Query("SELECT grl FROM GroupRoutineList grl JOIN grl.userInRooms uir WHERE uir.user = :user")
    Page<GroupRoutineList> findAllByUser(@Param("user") User user, Pageable pageable);

}
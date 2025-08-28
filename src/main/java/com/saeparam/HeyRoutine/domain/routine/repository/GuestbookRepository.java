package com.saeparam.HeyRoutine.domain.routine.repository;

import com.saeparam.HeyRoutine.domain.routine.entity.Guestbook;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saeparam.HeyRoutine.domain.routine.entity.GroupRoutineList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Repository
public interface GuestbookRepository extends JpaRepository<Guestbook, Long> {
    Page<Guestbook> findByGroupRoutineList(GroupRoutineList groupRoutineList, Pageable pageable);

    Optional<Guestbook> findByIdAndGroupRoutineList(Long id, GroupRoutineList groupRoutineList);

    void deleteAllByGroupRoutineList(GroupRoutineList groupRoutineList);

    /**
     * 특정 사용자가 작성한 모든 방명록을 삭제합니다.
     *
     * @param user 삭제 대상 사용자
     */
    void deleteAllByUser(User user);
}
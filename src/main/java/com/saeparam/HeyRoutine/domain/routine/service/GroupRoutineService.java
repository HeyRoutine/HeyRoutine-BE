package com.saeparam.HeyRoutine.domain.routine.service;

import org.springframework.data.domain.Pageable;

public interface GroupRoutineService {

    // 단체루틴 리스트 조회
    Object getGroupRoutines(String email, Pageable pageable);

    // 단체루틴 생성
    Object createGroupRoutine(String email, Object createDto);

    // 단체루틴 상세 조회
    Object getGroupRoutineDetail(String email, Long groupRoutineListId);

    // 단체루틴 수정
    Object updateGroupRoutine(String email, Long groupRoutineListId, Object updateDto);

    // 단체루틴 삭제
    void deleteGroupRoutine(String email, Long groupRoutineListId);

    // 단체루틴 가입
    Object joinGroupRoutine(String email, Long groupRoutineListId);

    // 단체루틴 상세 생성
    Object createGroupRoutineDetail(String email, Long groupRoutineListId, Object createDetailDto);

    // 단체루틴 상세 수정
    Object updateGroupRoutineDetail(String email, Long groupRoutineListId, Object updateDetailDto);

    // 단체루틴 상세 삭제
    void deleteGroupRoutineDetail(String email, Long groupRoutineListId, Long routineId);

    // 단체루틴 상세루틴 성공/실패
    Object updateGroupRoutineStatus(String email, Long groupRoutineListId, Long routineId, Object statusDto);

    // 방명록 조회
    Object getGroupGuestbooks(String email, Long groupRoutineListId, Pageable pageable);

    // 방명록 작성
    Object createGroupGuestbook(String email, Long groupRoutineListId, Object guestbookDto);

    // 방명록 삭제
    void deleteGroupGuestbook(String email, Long groupRoutineListId, Long guestbookId);
}
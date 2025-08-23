package com.saeparam.HeyRoutine.domain.routine.service;

import com.saeparam.HeyRoutine.domain.routine.dto.request.GroupRoutineRequestDto;
import com.saeparam.HeyRoutine.domain.routine.dto.request.GuestbookRequestDto;
import com.saeparam.HeyRoutine.domain.routine.dto.request.SubRoutineRequestDto;
import com.saeparam.HeyRoutine.domain.routine.dto.response.GroupRoutineResponseDto;
import com.saeparam.HeyRoutine.domain.routine.dto.response.GuestbookResponseDto;
import com.saeparam.HeyRoutine.global.web.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * <h2>GroupRoutineService</h2>
 * <p>
 * 단체 루틴 관련 비즈니스 로직을 처리하는 서비스 인터페이스입니다. <br>
 * Controller 레이어로부터 요청을 받아, 데이터 접근(Repository) 및 비즈니스 규칙 적용을 담당합니다.
 * </p>
 *
 * <pre>
 * ┌──────────────┐      DTOs      ┌─────────────┐      Entities      ┌──────────────┐
 * │  Controller  │◄──────────────►│   Service   │◄──────────────────►│  Repository  │
 * └──────────────┘                └─────────────┘                    └──────────────┘
 * </pre>
 */
public interface GroupRoutineService {

    /**
     * 페이징 처리된 단체 루틴 목록을 조회합니다.
     * <p>
     * 각 루틴에 대해 현재 사용자의 참여 여부(`isJoined`)를 함께 반환합니다.
     * </p>
     *
     * @param id    현재 로그인한 사용자의 uuid
     * @param pageable 페이지 번호, 페이지 크기 정보를 담은 객체
     * @return {@link GroupRoutineResponseDto.GroupRoutineInfo} 단체 루틴 목록 정보
     */
    PaginatedResponse<GroupRoutineResponseDto.GroupRoutineInfo> getGroupRoutines(UUID id, Pageable pageable);

    /**
     * 새로운 단체 루틴을 생성합니다.
     *
     * @param id     루틴을 생성하는 사용자(방장)의 uid
     * @param createDto 단체 루틴 생성을 위한 요청 데이터
     */
    void createGroupRoutine(UUID id, GroupRoutineRequestDto.Create createDto);

    /**
     * 특정 단체 루틴의 상세 정보를 조회합니다.
     * <p>
     * 사용자의 참여 상태(미참여, 참여, 방장)에 따라 반환되는 멤버 정보가 달라집니다.
     * </p>
     * <ul>
     *     <li><b>미참여자:</b> 참여중인 멤버들의 프로필 이미지 목록</li>
     *     <li><b>참여자/방장:</b> 루틴 성공/실패 인원 수 및 프로필 이미지 목록</li>
     * </ul>
     *
     * @param id              현재 로그인한 사용자의 uuid
     * @param groupRoutineListId 조회할 단체 루틴의 ID
     * @return {@link GroupRoutineResponseDto.DetailResponse} 단체 루틴 상세 정보
     */
    GroupRoutineResponseDto.DetailResponse getGroupRoutineDetail(UUID id, Long groupRoutineListId);

    /**
     * 기존 단체 루틴의 정보를 수정합니다.
     * <p>
     * 해당 루틴의 방장만 수정을 진행할 수 있습니다.
     * </p>
     *
     * @param id              현재 로그인한 사용자의 uuid
     * @param groupRoutineListId 수정할 단체 루틴의 ID
     * @param updateDto          단체 루틴 수정을 위한 요청 데이터
     */
    void updateGroupRoutine(UUID id, Long groupRoutineListId, GroupRoutineRequestDto.Update updateDto);

    /**
     * 단체 루틴을 삭제합니다.
     * <p>
     * 해당 루틴의 방장만 삭제를 진행할 수 있습니다.
     * </p>
     *
     * @param id              현재 로그인한 사용자의 uuid
     * @param groupRoutineListId 삭제할 단체 루틴의 ID
     */
    void deleteGroupRoutine(UUID id, Long groupRoutineListId);

    /**
     * 사용자가 단체 루틴에 참여(가입)합니다.
     *
     * @param id              현재 로그인한 사용자의 uuid
     * @param groupRoutineListId 참여할 단체 루틴의 ID
     */
    void joinGroupRoutine(UUID id, Long groupRoutineListId);

    /**
     * 단체 루틴에서 탈퇴합니다.
     * <p>
     * 방장(단체 루틴 생성자)은 탈퇴할 수 없으며, 참여중인 사용자만 가능합니다.
     * </p>
     *
     * @param id                현재 로그인한 사용자의 uuid
     * @param groupRoutineListId 탈퇴할 단체 루틴의 ID
     */
    void leaveGroupRoutine(UUID id, Long groupRoutineListId);

    /**
     * 단체 루틴의 완료 여부(성공/실패)를 기록합니다.
     * <p>
     * 해당 루틴에 참여중인 사용자만 완료 여부를 기록할 수 있습니다.
     * </p>
     *
     * @param id                현재 로그인한 사용자의 uuid
     * @param groupRoutineListId 완료 여부를 기록할 단체 루틴의 ID
     * @param recordUpdateDto    완료 여부 갱신을 위한 요청 데이터
     */
    void updateGroupRoutineRecord(UUID id, Long groupRoutineListId, GroupRoutineRequestDto.RecordUpdate recordUpdateDto);

    /**
     * 단체 루틴에 속한 상세 루틴들을 생성합니다.
     * <p>
     * 해당 루틴의 방장만 생성을 진행할 수 있습니다.
     * </p>
     *
     * @param id              현재 로그인한 사용자의 uuid
     * @param groupRoutineListId 상세 루틴을 추가할 단체 루틴의 ID
     * @param createDetailDto    상세 루틴 생성을 위한 요청 데이터
     */
    void createGroupSubRoutines(UUID id, Long groupRoutineListId, SubRoutineRequestDto.Create createDetailDto);

    /**
     * 단체 루틴에 속한 상세 루틴들을 수정합니다.
     * <p>
     * 해당 루틴의 방장만 수정을 진행할 수 있습니다.
     * </p>
     *
     * @param id              현재 로그인한 사용자의 uuid
     * @param groupRoutineListId 상세 루틴을 수정할 단체 루틴의 ID
     * @param updateDetailDto    상세 루틴 수정을 위한 요청 데이터
     */
    void updateGroupSubRoutines(UUID id, Long groupRoutineListId, SubRoutineRequestDto.Update updateDetailDto);


    /**
     * 단체 루틴에 속한 상세 루틴들을 삭제합니다.
     * <p>
     * 해당 루틴의 방장만 수정을 진행할 수 있습니다.
     * </p>
     *
     * @param id              현재 로그인한 사용자의 uuid
     * @param groupRoutineListId 상세 루틴을 수정할 단체 루틴의 ID
     * @param routineId          삭제할 상세 루틴의 ID
     */
    void deleteGroupSubRoutines(UUID id, Long groupRoutineListId, Long routineId);

    /**
     * 특정 상세 루틴의 완료 상태(성공/실패)를 변경합니다.
     * <p>
     * 해당 루틴에 참여중인 사용자만 상태를 변경할 수 있습니다.
     * </p>
     *
     * @param id              현재 로그인한 사용자의 uuid
     * @param groupRoutineListId 상태를 변경할 상세 루틴이 속한 단체 루틴의 ID
     * @param routineId          상태를 변경할 상세 루틴의 ID
     * @param statusDto          상세 루틴 상태 변경을 위한 요청 데이터
     */
    void updateGroupRoutineStatus(UUID id, Long groupRoutineListId, Long routineId, SubRoutineRequestDto.StatusUpdate statusDto);

    /**
     * 특정 단체 루틴의 방명록 목록을 페이징 처리하여 조회합니다.
     *
     * @param id              현재 로그인한 사용자의 uuid
     * @param groupRoutineListId 방명록을 조회할 단체 루틴의 ID
     * @param pageable           페이지 번호, 페이지 크기 정보를 담은 객체
     * @return {@link GuestbookResponseDto.GuestbookInfo} 방명록 목록 정보
     */
    PaginatedResponse<GuestbookResponseDto.GuestbookInfo> getGroupGuestbooks(UUID id, Long groupRoutineListId, Pageable pageable);

    /**
     * 특정 단체 루틴에 방명록을 작성합니다.
     * <p>
     * 해당 루틴에 참여중인 사용자만 작성이 가능합니다.
     * </p>
     *
     * @param id              현재 로그인한 사용자의 uuid
     * @param groupRoutineListId 방명록을 작성할 단체 루틴의 ID
     * @param guestbookDto       방명록 생성을 위한 요청 데이터
     * @return {@link GuestbookResponseDto.GuestbookInfo} 생성된 방명록 상세 정보
     */
    GuestbookResponseDto.GuestbookInfo createGroupGuestbook(UUID id, Long groupRoutineListId, GuestbookRequestDto.Create guestbookDto);

    /**
     * 특정 방명록을 삭제합니다.
     * <p>
     * 방명록을 작성한 본인만 삭제할 수 있습니다.
     * </p>
     *
     * @param id              현재 로그인한 사용자의 uuid
     * @param groupRoutineListId 삭제할 방명록이 속한 단체 루틴의 ID
     * @param guestbookId        삭제할 방명록의 ID
     */
    void deleteGroupGuestbook(UUID id, Long groupRoutineListId, Long guestbookId);
}
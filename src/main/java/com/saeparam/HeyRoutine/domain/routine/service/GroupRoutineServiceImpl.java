package com.saeparam.HeyRoutine.domain.routine.service;

import com.saeparam.HeyRoutine.domain.routine.dto.request.GroupRoutineRequestDto;
import com.saeparam.HeyRoutine.domain.routine.dto.request.GuestbookRequestDto;
import com.saeparam.HeyRoutine.domain.routine.dto.request.SubRoutineRequestDto;
import com.saeparam.HeyRoutine.domain.routine.dto.response.GroupRoutineResponseDto;
import com.saeparam.HeyRoutine.domain.routine.dto.response.GuestbookResponseDto;
import com.saeparam.HeyRoutine.domain.routine.entity.GroupRoutineList;
import com.saeparam.HeyRoutine.domain.routine.repository.GroupRoutinDaysRepository;
import com.saeparam.HeyRoutine.domain.routine.repository.GroupRoutineListRepository;
import com.saeparam.HeyRoutine.domain.routine.repository.GroupRoutineMiddleRepository;
import com.saeparam.HeyRoutine.domain.routine.repository.UserInRoomRepository;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import com.saeparam.HeyRoutine.domain.user.repository.UserRepository;
import com.saeparam.HeyRoutine.global.error.handler.UserHandler;
import com.saeparam.HeyRoutine.global.web.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupRoutineServiceImpl implements GroupRoutineService {

    private final GroupRoutineListRepository groupRoutineListRepository;
    private final GroupRoutineMiddleRepository groupRoutineMiddleRepository;
    private final UserInRoomRepository userInRoomRepository;
    private final GroupRoutinDaysRepository groupRoutinDaysRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public GroupRoutineResponseDto.ListResponse getGroupRoutines(UUID userId, Pageable pageable) {
        // 1. 사용자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 단체 루틴 목록 조회 (페이징)
        Page<GroupRoutineList> routinePage = groupRoutineListRepository.findAll(pageable);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        // 3. 각 루틴에 대한 정보 매핑
        List<GroupRoutineResponseDto.GroupRoutineInfo> items = routinePage.getContent().stream()
                .map(routine -> {
                    long routineNums = groupRoutineMiddleRepository.countByRoutineList(routine);
                    long peopleNums = userInRoomRepository.countByGroupRoutineList(routine);

                    boolean isJoined = routine.getUser().equals(user)
                            || userInRoomRepository.existsByGroupRoutineListAndUser(routine, user);

                    List<String> dayOfWeek = groupRoutinDaysRepository.findByGroupRoutineList(routine)
                            .stream()
                            .map(day -> day.getDayType().name())
                            .collect(Collectors.toList());

                    return GroupRoutineResponseDto.GroupRoutineInfo.builder()
                            .id(routine.getId())
                            .routineType(routine.getRoutineType())
                            .title(routine.getTitle())
                            .description(routine.getDescription())
                            .startTime(routine.getStartTime().format(formatter))
                            .endTime(routine.getEndTime().format(formatter))
                            .routineNums((int) routineNums)
                            .peopleNums((int) peopleNums)
                            .dayOfWeek(dayOfWeek)
                            .isJoined(isJoined)
                            .build();
                })
                .collect(Collectors.toList());

        return GroupRoutineResponseDto.ListResponse.builder()
                .items(items)
                .build();
    }

    @Override
    public void createGroupRoutine(UUID userId, GroupRoutineRequestDto.Create createDto) {
        // TODO: Implement logic
    }

    @Override
    @Transactional(readOnly = true)
    public GroupRoutineResponseDto.DetailResponse getGroupRoutineDetail(UUID userId, Long groupRoutineListId) {
        // TODO: Implement logic
        return null;
    }

    @Override
    public void updateGroupRoutine(UUID userId, Long groupRoutineListId, GroupRoutineRequestDto.Update updateDto) {
        // TODO: Implement logic
    }

    @Override
    public void deleteGroupRoutine(UUID userId, Long groupRoutineListId) {
        // TODO: Implement logic
    }

    @Override
    public void joinGroupRoutine(UUID userId, Long groupRoutineListId) {
        // TODO: Implement logic
    }

    @Override
    public void createGroupSubRoutines(UUID userId, Long groupRoutineListId, SubRoutineRequestDto.Create createDetailDto) {
        // TODO: Implement logic
    }

    @Override
    public void updateGroupSubRoutines(UUID userId, Long groupRoutineListId, SubRoutineRequestDto.Update updateDetailDto) {
        // TODO: Implement logic
    }

    @Override
    public void deleteGroupSubRoutines(UUID userId, Long groupRoutineListId, Long routineId) {
        // TODO: Implement logic
    }

    @Override
    public void updateGroupRoutineStatus(UUID userId, Long groupRoutineListId, Long routineId, SubRoutineRequestDto.StatusUpdate statusDto) {
        // TODO: Implement logic
    }

    @Override
    @Transactional(readOnly = true)
    public GuestbookResponseDto.GuestbookList getGroupGuestbooks(UUID userId, Long groupRoutineListId, Pageable pageable) {
        // TODO: Implement logic
        return null;
    }

    @Override
    public GuestbookResponseDto.GuestbookInfo createGroupGuestbook(UUID userId, Long groupRoutineListId, GuestbookRequestDto.Create guestbookDto) {
        // TODO: Implement logic
        return null;
    }

    @Override
    public void deleteGroupGuestbook(UUID userId, Long groupRoutineListId, Long guestbookId) {
        // TODO: Implement logic
    }
}
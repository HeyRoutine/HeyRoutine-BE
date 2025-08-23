package com.saeparam.HeyRoutine.domain.routine.service;

import com.saeparam.HeyRoutine.domain.routine.dto.request.GroupRoutineRequestDto;
import com.saeparam.HeyRoutine.domain.routine.dto.request.GuestbookRequestDto;
import com.saeparam.HeyRoutine.domain.routine.dto.request.RoutineRequestDto;
import com.saeparam.HeyRoutine.domain.routine.dto.request.SubRoutineRequestDto;
import com.saeparam.HeyRoutine.domain.routine.dto.response.GroupRoutineResponseDto;
import com.saeparam.HeyRoutine.domain.routine.dto.response.GuestbookResponseDto;
import com.saeparam.HeyRoutine.domain.routine.entity.*;
import com.saeparam.HeyRoutine.domain.routine.enums.DayType;
import com.saeparam.HeyRoutine.domain.routine.enums.RoutineType;
import com.saeparam.HeyRoutine.domain.routine.repository.*;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import com.saeparam.HeyRoutine.domain.user.repository.UserRepository;
import com.saeparam.HeyRoutine.global.error.handler.RoutineHandler;
import com.saeparam.HeyRoutine.global.error.handler.UserHandler;
import com.saeparam.HeyRoutine.global.web.response.PaginatedResponse;
import com.saeparam.HeyRoutine.global.web.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupRoutineServiceImpl implements GroupRoutineService {

    private final GroupRoutineListRepository groupRoutineListRepository;
    private final GroupRoutineMiddleRepository groupRoutineMiddleRepository;
    private final UserInRoomRepository userInRoomRepository;
    private final GroupRoutinDaysRepository groupRoutinDaysRepository;
    private final GuestbookRepository guestbookRepository;
    private final UserRepository userRepository;
    private final RoutineRepository routineRepository;
    private final EmojiRepository emojiRepository;
    private final TemplateRepository templateRepository;
    private final RoutineRecordRepository routineRecordRepository;

    // 요일 변환 로직은 DayType.from(String)에 위임

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<GroupRoutineResponseDto.GroupRoutineInfo> getGroupRoutines(UUID userId, Pageable pageable) {
        // 1. 사용자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 단체 루틴 목록 조회 (페이징)
        Page<GroupRoutineList> routinePage = groupRoutineListRepository.findAll(pageable);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        // 3. 각 루틴에 대한 정보 매핑 및 페이지네이션 응답 생성
        return PaginatedResponse.of(routinePage, routine -> {
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
        });
    }

    @Override
    public void createGroupRoutine(UUID userId, GroupRoutineRequestDto.Create createDto) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 루틴 타입 유효성 검사
        RoutineType routineType = createDto.getRoutineType();
        if (routineType == null) {
            throw new RoutineHandler(ErrorStatus.INVALID_ROUTINE_TYPE);
        }

        // 3. 시작 및 종료 시간 파싱
        LocalTime startTime = parseTime(createDto.getStartTime());
        LocalTime endTime = parseTime(createDto.getEndTime());

        // 4. 요일 정보 변환
        List<DayType> dayTypes = convertDayTypes(createDto.getDaysOfWeek());

        // 5. GroupRoutineList 생성
        GroupRoutineList groupRoutineList = GroupRoutineList.builder()
                .user(user)
                .routineType(routineType)
                .title(createDto.getTitle())
                .description(createDto.getDescription())
                .startTime(startTime)
                .endTime(endTime)
                .userCnt(1) // 생성자 1명이니까~
                .build();

        groupRoutineListRepository.save(groupRoutineList);

        // 각 요일에 대해 GroupRoutineDays 엔티티 생성 및 저장
        for (DayType day : dayTypes) {
            groupRoutinDaysRepository.save(GroupRoutineDays.builder()
                    .groupRoutineList(groupRoutineList)
                    .dayType(day)
                    .build());
        }

        // 참여 정보 저장 -> 반장이지만 해당 그룹에 속해있으니
        userInRoomRepository.save(UserInRoom.builder()
                .groupRoutineList(groupRoutineList)
                .user(user)
                .build());
    }

    // 주석 다 쓰려니까 힘드네오 필요한 부분 간략할게 작성할게욥

    @Override
    public void updateGroupRoutine(UUID userId, Long groupRoutineListId, GroupRoutineRequestDto.Update updateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        GroupRoutineList groupRoutineList = groupRoutineListRepository.findById(groupRoutineListId)
                .orElseThrow(() -> new RoutineHandler(ErrorStatus.GROUP_ROUTINE_NOT_FOUND));

        // 403 - 루틴 권한 체크
        if (!groupRoutineList.getUser().equals(user)) {
            throw new RoutineHandler(ErrorStatus.ROUTINE_FORBIDDEN);
        }

        RoutineType routineType = updateDto.getRoutineType();
        if (routineType == null) {
            throw new RoutineHandler(ErrorStatus.INVALID_ROUTINE_TYPE);
        }

        LocalTime startTime = parseTime(updateDto.getStartTime());
        LocalTime endTime = parseTime(updateDto.getEndTime());

        List<DayType> dayTypes = convertDayTypes(updateDto.getDaysOfWeek());

        // 수정이니까 업데이트
        groupRoutineList.update(updateDto.getTitle(), updateDto.getDescription(), routineType, startTime, endTime);

        // "기존 요일 정보 삭제 후" 새로 저장
        groupRoutinDaysRepository.deleteAllByGroupRoutineList(groupRoutineList);
        for (DayType day : dayTypes) {
            groupRoutinDaysRepository.save(GroupRoutineDays.builder()
                    .groupRoutineList(groupRoutineList)
                    .dayType(day)
                    .build());
        }
    }

    @Override
    public void deleteGroupRoutine(UUID userId, Long groupRoutineListId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        GroupRoutineList groupRoutineList = groupRoutineListRepository.findById(groupRoutineListId)
                .orElseThrow(() -> new RoutineHandler(ErrorStatus.GROUP_ROUTINE_NOT_FOUND));

        // 단체루틴 당사자만 삭제가능해야하니
        if (!groupRoutineList.getUser().equals(user)) {
            throw new RoutineHandler(ErrorStatus.ROUTINE_FORBIDDEN);
        }

        // 연관된 데이터 삭제 (참조 무결성을 위해 순서대로 삭제)
        groupRoutinDaysRepository.deleteAllByGroupRoutineList(groupRoutineList);
        groupRoutineMiddleRepository.deleteAllByRoutineList(groupRoutineList);
        userInRoomRepository.deleteAllByGroupRoutineList(groupRoutineList);
        guestbookRepository.deleteAllByGroupRoutineList(groupRoutineList);

        // 단체 루틴 삭제
        groupRoutineListRepository.delete(groupRoutineList);
    }

    @Override
    public void joinGroupRoutine(UUID userId, Long groupRoutineListId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        GroupRoutineList groupRoutineList = groupRoutineListRepository.findById(groupRoutineListId)
                .orElseThrow(() -> new RoutineHandler(ErrorStatus.GROUP_ROUTINE_NOT_FOUND));

        // 이미 참여중인지 확인 (방장 포함)
        boolean isOwner = groupRoutineList.getUser().equals(user);
        boolean isMember = userInRoomRepository.existsByGroupRoutineListAndUser(groupRoutineList, user);
        if (isOwner || isMember) {
            throw new RoutineHandler(ErrorStatus.ALREADY_JOINED_ROUTINE);
        }

        // 참여 정보 저장
        userInRoomRepository.save(UserInRoom.builder()
                .groupRoutineList(groupRoutineList)
                .user(user)
                .build());

        // 인원 수 +1 해주기
        groupRoutineList.increaseUserCnt();
    }

    @Override
    @Transactional(readOnly = true)
    public GroupRoutineResponseDto.DetailResponse getGroupRoutineDetail(UUID userId, Long groupRoutineListId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        GroupRoutineList groupRoutineList = groupRoutineListRepository.findById(groupRoutineListId)
                .orElseThrow(() -> new RoutineHandler(ErrorStatus.GROUP_ROUTINE_NOT_FOUND));

        boolean isAdmin = groupRoutineList.getUser().equals(user);
        boolean isMember = userInRoomRepository.existsByGroupRoutineListAndUser(groupRoutineList, user);
        boolean isJoined = isAdmin || isMember;

        // 기본 정보 구성
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        long routineNums = groupRoutineMiddleRepository.countByRoutineList(groupRoutineList);
        long peopleNums = userInRoomRepository.countByGroupRoutineList(groupRoutineList);
        List<String> dayOfWeek = groupRoutinDaysRepository.findByGroupRoutineList(groupRoutineList)
                .stream()
                .map(day -> day.getDayType().name())
                .collect(Collectors.toList());

        GroupRoutineResponseDto.GroupRoutineInfo routineInfo = GroupRoutineResponseDto.GroupRoutineInfo.builder()
                .id(groupRoutineList.getId())
                .routineType(groupRoutineList.getRoutineType())
                .title(groupRoutineList.getTitle())
                .description(groupRoutineList.getDescription())
                .startTime(groupRoutineList.getStartTime().format(formatter))
                .endTime(groupRoutineList.getEndTime().format(formatter))
                .routineNums((int) routineNums)
                .peopleNums((int) peopleNums)
                .dayOfWeek(dayOfWeek)
                .isJoined(isJoined)
                .build();

        // 상세 루틴 정보
        List<GroupRoutineMiddle> middles = groupRoutineMiddleRepository.findWithRoutineByRoutineList(groupRoutineList);
        List<Routine> routines = middles.stream().map(GroupRoutineMiddle::getRoutine).collect(Collectors.toList());

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        Set<Long> completedIds;
        if (isJoined && !routines.isEmpty()) {
            List<RoutineRecord> records = routineRecordRepository.findRecordsByDateAndRoutines(user, startOfDay, endOfDay, routines);
            completedIds = records.stream()
                    .filter(RoutineRecord::isDoneCheck)
                    .map(rr -> rr.getRoutine().getId())
                    .collect(Collectors.toSet());
        } else {
            completedIds = new HashSet<>();
        }

        List<GroupRoutineResponseDto.RoutineInfo> routineInfos = routines.stream()
                .map(r -> GroupRoutineResponseDto.RoutineInfo.builder()
                        .id(r.getId())
                        .emojiId(r.getEmoji().getId())
                        .name(r.getName())
                        .time(r.getTime())
                        .isCompleted(isJoined ? completedIds.contains(r.getId()) : null)
                        .build())
                .collect(Collectors.toList());

        // 참여자 정보
        List<UserInRoom> members = userInRoomRepository.findByGroupRoutineList(groupRoutineList);
        List<User> memberUsers = members.stream().map(UserInRoom::getUser).collect(Collectors.toList());

        GroupRoutineResponseDto.GroupRoutineMemberInfo memberInfo;
        if (!isJoined) {
            List<String> profileUrls = memberUsers.stream()
                    .map(User::getProfileImage)
                    .filter(Objects::nonNull)
                    .limit(8)
                    .collect(Collectors.toList());
            memberInfo = GroupRoutineResponseDto.GroupRoutineMemberInfo.builder()
                    .profileImageUrl(profileUrls)
                    .build();
        } else {
            int successCnt = 0;
            int failedCnt = 0;
            List<String> successUrls = new ArrayList<>();
            List<String> failedUrls = new ArrayList<>();

            for (User member : memberUsers) {
                List<RoutineRecord> records = routineRecordRepository.findRecordsByDateAndRoutines(member, startOfDay, endOfDay, routines);
                long doneSize = records.stream().filter(RoutineRecord::isDoneCheck).map(rr -> rr.getRoutine().getId()).distinct().count();
                boolean allCompleted = doneSize == routines.size() && !routines.isEmpty();

                if (allCompleted) {
                    successCnt++;
                    if (successUrls.size() < 8 && member.getProfileImage() != null) {
                        successUrls.add(member.getProfileImage());
                    }
                } else {
                    failedCnt++;
                    if (failedUrls.size() < 8 && member.getProfileImage() != null) {
                        failedUrls.add(member.getProfileImage());
                    }
                }
            }

            memberInfo = GroupRoutineResponseDto.GroupRoutineMemberInfo.builder()
                    .successPeopleNums(successCnt)
                    .successPeopleProfileImageUrl(successUrls)
                    .failedPeopleNums(failedCnt)
                    .failedPeopleProfileImageUrl(failedUrls)
                    .build();
        }

        return GroupRoutineResponseDto.DetailResponse.builder()
                .isAdmin(isAdmin)
                .groupRoutineInfo(routineInfo)
                .routineInfos(routineInfos)
                .groupRoutineMemberInfo(memberInfo)
                .build();
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
    public PaginatedResponse<GuestbookResponseDto.GuestbookInfo> getGroupGuestbooks(UUID userId, Long groupRoutineListId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        GroupRoutineList groupRoutineList = groupRoutineListRepository.findById(groupRoutineListId)
                .orElseThrow(() -> new RoutineHandler(ErrorStatus.GROUP_ROUTINE_NOT_FOUND));

        // 해당 단체루틴에 속한 유저만 방명록 볼 수 있어야하니까 예외
        boolean isMember = userInRoomRepository.existsByGroupRoutineListAndUser(groupRoutineList, user);
        if (!isMember) {
            throw new RoutineHandler(ErrorStatus.GUESTBOOK_GET_FORBIDDEN);
        }

        Page<Guestbook> guestbookPage = guestbookRepository.findByGroupRoutineList(groupRoutineList, pageable);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return PaginatedResponse.of(guestbookPage, gb -> GuestbookResponseDto.GuestbookInfo.builder()
                .id(gb.getId())
                .userId(gb.getUser().getId())
                .nickname(gb.getUser().getNickname())
                .profileImageUrl(gb.getUser().getProfileImage())
                .content(gb.getContent())
                .createdAt(gb.getCreatedDate() != null ? gb.getCreatedDate().format(formatter) : null)
                .isWriter(gb.getUser().equals(user))
                .build());
    }

    @Override
    public GuestbookResponseDto.GuestbookInfo createGroupGuestbook(UUID userId, Long groupRoutineListId, GuestbookRequestDto.Create guestbookDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        GroupRoutineList groupRoutineList = groupRoutineListRepository.findById(groupRoutineListId)
                .orElseThrow(() -> new RoutineHandler(ErrorStatus.GROUP_ROUTINE_NOT_FOUND));

        boolean isMember = groupRoutineList.getUser().equals(user) ||
                userInRoomRepository.existsByGroupRoutineListAndUser(groupRoutineList, user);
        if (!isMember) {
            throw new RoutineHandler(ErrorStatus.GUESTBOOK_FORBIDDEN);
        }

        Guestbook guestbook = Guestbook.builder()
                .user(user)
                .groupRoutineList(groupRoutineList)
                .content(guestbookDto.getContent())
                .build();
        Guestbook saved = guestbookRepository.save(guestbook);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return GuestbookResponseDto.GuestbookInfo.builder()
                .id(saved.getId())
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImage())
                .content(saved.getContent())
                .createdAt(saved.getCreatedDate() != null ? saved.getCreatedDate().format(formatter) : null)
                .isWriter(true)
                .build();
    }

    @Override
    public void deleteGroupGuestbook(UUID userId, Long groupRoutineListId, Long guestbookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        GroupRoutineList groupRoutineList = groupRoutineListRepository.findById(groupRoutineListId)
                .orElseThrow(() -> new RoutineHandler(ErrorStatus.GROUP_ROUTINE_NOT_FOUND));

        Guestbook guestbook = guestbookRepository.findByIdAndGroupRoutineList(guestbookId, groupRoutineList)
                .orElseThrow(() -> new RoutineHandler(ErrorStatus.GUESTBOOK_NOT_FOUND));

        if (!guestbook.getUser().equals(user)) {
            throw new RoutineHandler(ErrorStatus.GUESTBOOK_FORBIDDEN);
        }

        guestbookRepository.delete(guestbook);
    }


    // ####################### Private 서브 메서드 #######################
    /**
     * 문자열 형태의 시간을 {@link LocalTime}으로 변환합니다.
     *
     * @param time HH:mm 형식의 문자열
     * @return {@link LocalTime}
     * @throws RoutineHandler 시간이 올바른 형식이 아닐 경우
     */
    private LocalTime parseTime(String time) {
        try {
            return LocalTime.parse(time);
        } catch (DateTimeParseException e) {
            throw new RoutineHandler(ErrorStatus.INVALID_TIME_FORMAT);
        }
    }

    /**
     * 요일 문자열 리스트를 {@link DayType} 리스트로 변환합니다.
     * 중복되거나 존재하지 않는 요일이 포함된 경우 예외가 발생합니다.
     *
     * @param daysOfWeek 요일 문자열 리스트
     * @return 변환된 요일 리스트
     * @throws RoutineHandler 요일 정보가 중복되거나 올바르지 않은 경우
     */
    private List<DayType> convertDayTypes(List<String> daysOfWeek) {
        if (daysOfWeek == null || daysOfWeek.isEmpty()) {
            throw new RoutineHandler(ErrorStatus.INVALID_DAY_OF_WEEK);
        }

        Set<DayType> resultSet = new LinkedHashSet<>();
        for (String day : daysOfWeek) {
            DayType dayType = DayType.from(day);
            if (dayType == null || !resultSet.add(dayType)) {
                throw new RoutineHandler(ErrorStatus.INVALID_DAY_OF_WEEK);
            }
        }

        return resultSet.stream().collect(Collectors.toList());
    }
}
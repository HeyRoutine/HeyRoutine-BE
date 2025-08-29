package com.saeparam.HeyRoutine.domain.user.service;


import com.saeparam.HeyRoutine.domain.fcm.repository.FcmTokenRepository;
import com.saeparam.HeyRoutine.domain.routine.entity.GroupRoutineList;
import com.saeparam.HeyRoutine.domain.routine.entity.GroupRoutineMiddle;
import com.saeparam.HeyRoutine.domain.routine.entity.UserInRoom;
import com.saeparam.HeyRoutine.domain.routine.entity.MyRoutineList;
import com.saeparam.HeyRoutine.domain.routine.repository.GroupRoutineListDoneCheckRepository;
import com.saeparam.HeyRoutine.domain.routine.repository.GroupRoutineListRepository;
import com.saeparam.HeyRoutine.domain.routine.repository.GroupRoutineMiddleRepository;
import com.saeparam.HeyRoutine.domain.routine.repository.GroupRoutinDaysRepository;
import com.saeparam.HeyRoutine.domain.routine.repository.GuestbookRepository;
import com.saeparam.HeyRoutine.domain.routine.repository.MyRoutineListRecordRepository;
import com.saeparam.HeyRoutine.domain.routine.repository.MyRoutineListRepository;
import com.saeparam.HeyRoutine.domain.routine.repository.RoutineRecordRepository;
import com.saeparam.HeyRoutine.domain.routine.repository.RoutineRepository;
import com.saeparam.HeyRoutine.domain.routine.repository.UserInRoomRepository;
import com.saeparam.HeyRoutine.domain.user.dto.request.SurveyRequestDto;
import com.saeparam.HeyRoutine.domain.user.dto.response.MyInfoResponseDto;
import com.saeparam.HeyRoutine.domain.user.dto.response.SearchInfoDto;
import com.saeparam.HeyRoutine.domain.user.entity.Major;
import com.saeparam.HeyRoutine.domain.user.entity.University;
import com.saeparam.HeyRoutine.domain.user.entity.UserSurveyFlags;
import com.saeparam.HeyRoutine.domain.user.repository.MajorRepository;
import com.saeparam.HeyRoutine.domain.user.repository.UniversityRepository;
import com.saeparam.HeyRoutine.domain.user.repository.UserSurveyFlagsRepository;
import com.saeparam.HeyRoutine.domain.user.service.event.UserSignedUpEvent;
import com.saeparam.HeyRoutine.global.error.handler.TokenHandler;
import com.saeparam.HeyRoutine.global.error.handler.UserHandler;
import com.saeparam.HeyRoutine.global.infra.http.bank.WebClientBankUtil;
import com.saeparam.HeyRoutine.global.web.response.code.status.ErrorStatus;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import com.saeparam.HeyRoutine.domain.user.dto.JwtToken;
import com.saeparam.HeyRoutine.domain.user.dto.request.ReissueDto;
import com.saeparam.HeyRoutine.domain.user.dto.request.ResetPasswordDto;
import com.saeparam.HeyRoutine.domain.user.dto.request.SignUpDto;
import com.saeparam.HeyRoutine.domain.user.dto.response.UserDto;
import com.saeparam.HeyRoutine.domain.user.repository.UserRepository;
import com.saeparam.HeyRoutine.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;
    private final WebClientBankUtil webClientBankUtil;
    private final ApplicationEventPublisher eventPublisher;
    private final FcmTokenRepository fcmTokenRepository;
    private final UserSurveyFlagsRepository userSurveyFlagsRepository;
    private final MyRoutineListRepository myRoutineListRepository;
    private final MyRoutineListRecordRepository myRoutineListRecordRepository;
    private final RoutineRepository routineRepository;
    private final RoutineRecordRepository routineRecordRepository;
    private final GroupRoutineListRepository groupRoutineListRepository;
    private final GroupRoutinDaysRepository groupRoutinDaysRepository;
    private final GroupRoutineListDoneCheckRepository groupRoutineListDoneCheckRepository;
    private final GroupRoutineMiddleRepository groupRoutineMiddleRepository;
    private final UserInRoomRepository userInRoomRepository;
    private final GuestbookRepository guestbookRepository;
    private final UniversityRepository universityRepository;
    private final MajorRepository majorRepository;


    @Transactional
    public JwtToken signIn(String username, String password) {

        // 1. username + password 를 기반으로 Authentication 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        try {

            // 2. 실제 검증. authenticate() 메서드를 통해 요청된 Master 에 대한 검증 진행
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

            // 3. 인증 정보를 기반으로 JWT 토큰 생성
//            JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);
            JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);

            // Refresh Token을 Redis에 저장
            redisTemplate.opsForValue()
                    .set("RT:" + authentication.getName(), jwtToken.getRefreshToken(), jwtToken.getRefreshTokenExpirationTime(), TimeUnit.MILLISECONDS);

            log.info("[signIn] 로그인 성공: username = {}", username);
            return jwtToken;
        } catch (BadCredentialsException e) {
            log.error("[signIn] 로그인 실패: 잘못된 아이디 및 비밀번호, username = {}", username);
            throw new UserHandler(ErrorStatus.USER_INVALID_CREDENTIALS);  // 'INVALID_CREDENTIALS' 에러 코드로 구체적인 비밀번호 오류 처리
        } catch (Exception e) {
            log.error("[signIn] 로그인 실패: username = {}, 오류 = {}", username, e.getMessage());
            throw new UserHandler(ErrorStatus._INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Search universities by keyword.
     *
     * @param keyword 검색할 키워드
     * @return 검색 결과 리스트
     */
    @Transactional(readOnly = true)
    public List<SearchInfoDto> searchUniversities(String keyword) {
        return universityRepository.findTop10ByNameContainingIgnoreCase(keyword).stream()
            .map(SearchInfoDto::fromUniversity)
            .toList();
    }

    /**
     * Search majors by keyword.
     *
     * @param keyword 검색할 키워드
     * @return 검색 결과 리스트
     */
    @Transactional(readOnly = true)
    public List<SearchInfoDto> searchMajors(String keyword) {
        return majorRepository.findTop10ByNameContainingIgnoreCase(keyword).stream()
            .map(SearchInfoDto::fromMajor)
            .toList();
    }

    @Transactional
    public UserDto signUp(SignUpDto signUpDto) {
        log.info("[signUp] 회원가입 요청: username = {}", signUpDto.getEmail());
        checkEmailDuplicate(signUpDto.getEmail());
        checknicknameDuplicate(signUpDto.getNickname());

        // Password 암호화
        String encodedPassword = passwordEncoder.encode(signUpDto.getPassword());
        University university = universityRepository.findById(signUpDto.getUniversityId())
            .orElseThrow(() -> new UserHandler(ErrorStatus.UNIVERSITY_NOT_FOUND));
        Major major = majorRepository.findById(signUpDto.getMajorId())
            .orElseThrow(() -> new UserHandler(ErrorStatus.MAJOR_NOT_FOUND));

        // 회원가입 성공 처리
        User user = signUpDto.toEntity(encodedPassword, university, major);
        user.setAccountCertificationStatus(false); // account_certification_status를 false로 설정
        UserDto userDto = UserDto.toDto(userRepository.save(user));
        eventPublisher.publishEvent(new UserSignedUpEvent(signUpDto.getEmail()));

        return userDto;
    }

    @Transactional
    public JwtToken reissue(ReissueDto reissueDto) {
        log.info("[reissue] 토큰 갱신 요청: accessToken = {}", reissueDto.getAccessToken());


        // RefreshToken 검증
        if (!jwtTokenProvider.validateToken(reissueDto.getRefreshToken())) {
            log.warn("[reissue] RefreshToken 유효하지 않음: refreshToken = {}", reissueDto.getRefreshToken());
            throw new TokenHandler(ErrorStatus.REFRESH_TOKEN_NOT_VALID);
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(reissueDto.getAccessToken());
        String refreshToken = (String) redisTemplate.opsForValue().get("RT:" + authentication.getName());

        if (refreshToken == null) {
            throw new TokenHandler(ErrorStatus.REFRESH_TOKEN_EXPIRED);
        }

        if (!refreshToken.equals(reissueDto.getRefreshToken())) {
            log.warn("[reissue] RefreshToken 불일치: username = {}", authentication.getName());
            throw new TokenHandler(ErrorStatus.REFRESH_TOKEN_NOT_MATCH);
        }

        // 새 JWT 토큰 생성
        JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);

        // RefreshToken Redis 업데이트
        redisTemplate.opsForValue()
                .set("RT:" + authentication.getName(), jwtToken.getRefreshToken(), jwtToken.getRefreshTokenExpirationTime(), TimeUnit.MILLISECONDS);

        log.info("[reissue] 토큰 갱신 성공: username = {}", authentication.getName());
        return jwtToken;
    }

    public MyInfoResponseDto myInfo(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        return MyInfoResponseDto.builder()
                .profileImage(user.getProfileImage())
                .nickname(user.getNickname())
                .bankAccount(user.getBankAccount())
                .point(user.getPoint())
                .isMarketing(user.getIsMarketing())
                .accountCertificationStatus(user.getAccountCertificationStatus())
                .build();
    }

    @Transactional
    public String checkEmailDuplicate(String email) {
        // 이메일 중복체크
        if (userRepository.existsByEmail(email)) {
            throw new UserHandler(ErrorStatus.USER_ID_IN_USE);
        }
        return "사용가능한 이메일입니다";

    }

    @Transactional
    public String checknicknameDuplicate(String nickname) {
        // 닉네임 중복체크
        if (userRepository.existsByNickname(nickname)) {
            throw new UserHandler(ErrorStatus.USER_NICKNAME_IN_USE);
        }
        return "사용가능한 닉네임입니다";
    }

    @Transactional
    public String resetPassword(ResetPasswordDto resetPasswordDto) {
        User user = userRepository.findByEmail(resetPasswordDto.getEmail())
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        String uuid = (String) redisTemplate.opsForValue().get("UUID:" + resetPasswordDto.getUuid());
        if (uuid == null || !uuid.equals(resetPasswordDto.getEmail())) {
            throw new UserHandler(ErrorStatus.USER_NOT_AUTHORITY);
        }
        String encodedPassword = passwordEncoder.encode(resetPasswordDto.getPassword());
        user.setPassword(encodedPassword);

        redisTemplate.delete("UUID:" + resetPasswordDto.getUuid());
        return "비밀번호가 변경되었습니다.";
    }

    @Transactional
    public void mypageResetPassword(UUID userId, String exsPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        String encodedExistingPassword = user.getPassword();
        String encodedExsPassword = passwordEncoder.encode(exsPassword);
        String encodedNewPassword = passwordEncoder.encode(newPassword);

        // 기존 비밀번호 검증
        if (!passwordEncoder.matches(exsPassword, encodedExistingPassword)) {
            throw new UserHandler(ErrorStatus.PASSWORD_NOT_MATCH);
        }

        // 새 비밀번호가 기존과 동일한지 확인
        if (passwordEncoder.matches(newPassword, encodedExistingPassword) ||
                passwordEncoder.matches(newPassword, encodedExsPassword)) {
            throw new UserHandler(ErrorStatus.PASSWORD_SAME_AS_OLD);
        }

        user.setPassword(encodedNewPassword);
    }

    /**
     * 닉네임 변경
     *
     * @param userId
     * @param nickname
     * @return
     */

    @Transactional
    public String mypageResetNickname(UUID userId, String nickname) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        if (userRepository.existsByNickname(nickname)) {
            throw new UserHandler(ErrorStatus.USER_NICKNAME_IN_USE);
        }
        user.setNickname(nickname);

        return "닉네임이 변경되었습니다";
    }

    /**
     * 마케팅 수신 여부 업데이트
     *
     * @param userId     사용자 ID
     * @param isMarketing 마케팅 수신 여부
     */
    @Transactional
    public void updateIsMarketing(UUID userId, boolean isMarketing) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        user.setMarketing(isMarketing);
        userRepository.save(user);
    }

    /**
     * 프로필 이미지 변경
     * @param userId 사용자 식별자
     * @param profileImageUrl 변경할 프로필 이미지 URL
     */
    @Transactional
    public void updateProfileImage(UUID userId, String profileImageUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        user.setProfileImage(profileImageUrl);
    }


    @Transactional
    public String logout(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        if (redisTemplate.opsForValue().get("RT:" + user.getId()) != null) {
            redisTemplate.delete("RT:" + user.getId());
        }
        fcmTokenRepository.deleteAllByUser(user);

        return "로그아웃 되었습니다.";
    }

    @Transactional
    public void survey(UUID userId, SurveyRequestDto surveyRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        // 3. UserSurveyFlags 엔티티 빌더 생성
        UserSurveyFlags.UserSurveyFlagsBuilder builder = UserSurveyFlags.builder()
                .userId(user.getId().toString());

        List<Boolean> surveyList = surveyRequestDto.getSurveyList();


        // 3. userId로 기존 설문 데이터가 있는지 조회
        Optional<UserSurveyFlags> existingSurvey = userSurveyFlagsRepository.findByUserId(user.getId().toString());

        if (existingSurvey.isPresent()) {
            // 4-1. 데이터가 이미 존재하면 -> 업데이트 (더티 체킹 활용)
            UserSurveyFlags surveyToUpdate = existingSurvey.get();
            surveyToUpdate.updateFlags(surveyList);
            // @Transactional에 의해 메소드 종료 시 자동으로 UPDATE 쿼리 실행됨
        } else {
            // 4-2. 데이터가 없으면 -> 새로 생성 및 저장
            UserSurveyFlags newSurvey = UserSurveyFlags.builder()
                    .userId(user.getId().toString())
                    .q0(surveyList.get(0)).q1(surveyList.get(1)).q2(surveyList.get(2))
                    .q3(surveyList.get(3)).q4(surveyList.get(4)).q5(surveyList.get(5))
                    .q6(surveyList.get(6)).q7(surveyList.get(7)).q8(surveyList.get(8))
                    .q9(surveyList.get(9)).q10(surveyList.get(10)).q11(surveyList.get(11))
                    .q12(surveyList.get(12)).q13(surveyList.get(13)).q14(surveyList.get(14))
                    .q15(surveyList.get(15)).q16(surveyList.get(16)).q17(surveyList.get(17))
                    .q18(surveyList.get(18)).q19(surveyList.get(19)).q20(surveyList.get(20))
                    .q21(surveyList.get(21)).q22(surveyList.get(22)).q23(surveyList.get(23))
                    .q24(surveyList.get(24)).q25(surveyList.get(25)).q26(surveyList.get(26))
                    .q27(surveyList.get(27)).q28(surveyList.get(28)).q29(surveyList.get(29))
                    .q30(surveyList.get(30)).q31(surveyList.get(31)).q32(surveyList.get(32))
                    .build();
            userSurveyFlagsRepository.save(newSurvey);
        }
    }


    /**
     * 회원 탈퇴 처리
     *
     * <p>사용자가 탈퇴할 때 개인 루틴, 단체 루틴 참여 정보, 관련 기록 및 FCM 토큰 등을
     * 모두 삭제한 뒤 최종적으로 사용자 정보를 제거합니다.</p>
     *
     * @param userId 탈퇴할 사용자 ID
     */
    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // Refresh Token 제거
        if (redisTemplate.opsForValue().get("RT:" + user.getId()) != null) {
            redisTemplate.delete("RT:" + user.getId());
        }

        // FCM 토큰 제거
        fcmTokenRepository.deleteAllByUser(user);

        // 개인 루틴 및 기록 삭제
        myRoutineListRecordRepository.deleteAllByUser(user);
        List<MyRoutineList> myRoutineLists = myRoutineListRepository.findAllByUser(user);
        myRoutineListRepository.deleteAll(myRoutineLists);

        // 사용자가 방장인 단체 루틴 삭제
        List<GroupRoutineList> ownedGroups = groupRoutineListRepository.findAllByUser(user);
        for (GroupRoutineList group : ownedGroups) {
            guestbookRepository.deleteAllByGroupRoutineList(group);
            groupRoutineListDoneCheckRepository.deleteAllByGroupRoutineList(group);
            userInRoomRepository.deleteAllByGroupRoutineList(group);

            List<GroupRoutineMiddle> middles = groupRoutineMiddleRepository.findByRoutineList(group);
            for (GroupRoutineMiddle middle : middles) {
                routineRecordRepository.deleteAllByRoutine(middle.getRoutine());
                routineRepository.delete(middle.getRoutine());
            }
            groupRoutineMiddleRepository.deleteAllByRoutineList(group);
            groupRoutinDaysRepository.deleteAllByGroupRoutineList(group);
            groupRoutineListRepository.delete(group);
        }

        // 사용자가 참여중인 단체 루틴에서 제거
        List<UserInRoom> joinedRooms = userInRoomRepository.findAllByUser(user);
        for (UserInRoom userInRoom : joinedRooms) {
            GroupRoutineList group = userInRoom.getGroupRoutineList();
            groupRoutineListDoneCheckRepository.deleteByGroupRoutineListAndUser(group, user);
            userInRoomRepository.delete(userInRoom);
            group.decreaseUserCnt();
        }
        userInRoomRepository.deleteAllByUser(user);

        // 사용자가 작성한 기타 데이터 정리
        guestbookRepository.deleteAllByUser(user);
        groupRoutineListDoneCheckRepository.deleteAllByUser(user);
        routineRecordRepository.deleteAllByUser(user);

        // 최종적으로 사용자 삭제
        userRepository.delete(user);
    }
}

package com.saeparam.HeyRoutine.domain.user.service;


import com.saeparam.HeyRoutine.domain.fcm.repository.FcmTokenRepository;
import com.saeparam.HeyRoutine.domain.user.dto.request.SurveyRequestDto;
import com.saeparam.HeyRoutine.domain.user.dto.response.MyInfoResponseDto;
import com.saeparam.HeyRoutine.domain.user.entity.UserSurveyFlags;
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

    @Transactional
    public UserDto signUp(SignUpDto signUpDto) {
        log.info("[signUp] 회원가입 요청: username = {}", signUpDto.getEmail());
        checkEmailDuplicate(signUpDto.getEmail());
        checknicknameDuplicate(signUpDto.getNickname());
        // Password 암호화
        String encodedPassword = passwordEncoder.encode(signUpDto.getPassword());

        // 회원가입 성공 처리
        UserDto userDto = UserDto.toDto(userRepository.save(signUpDto.toEntity(signUpDto, encodedPassword)));
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
        

        // 4. 리스트의 값을 각 필드에 매핑
        builder.q0(surveyRequestDto.getSurveyList().get(0));
        builder.q1(surveyRequestDto.getSurveyList().get(1));
        builder.q2(surveyRequestDto.getSurveyList().get(2));
        builder.q3(surveyRequestDto.getSurveyList().get(3));
        builder.q4(surveyRequestDto.getSurveyList().get(4));
        builder.q5(surveyRequestDto.getSurveyList().get(5));
        builder.q6(surveyRequestDto.getSurveyList().get(6));
        builder.q7(surveyRequestDto.getSurveyList().get(7));
        builder.q8(surveyRequestDto.getSurveyList().get(8));
        builder.q9(surveyRequestDto.getSurveyList().get(9));
        builder.q10(surveyRequestDto.getSurveyList().get(10));
        builder.q11(surveyRequestDto.getSurveyList().get(11));
        builder.q12(surveyRequestDto.getSurveyList().get(12));
        builder.q13(surveyRequestDto.getSurveyList().get(13));
        builder.q14(surveyRequestDto.getSurveyList().get(14));
        builder.q15(surveyRequestDto.getSurveyList().get(15));
        builder.q16(surveyRequestDto.getSurveyList().get(16));
        builder.q17(surveyRequestDto.getSurveyList().get(17));
        builder.q18(surveyRequestDto.getSurveyList().get(18));
        builder.q19(surveyRequestDto.getSurveyList().get(19));
        builder.q20(surveyRequestDto.getSurveyList().get(20));
        builder.q21(surveyRequestDto.getSurveyList().get(21));
        builder.q22(surveyRequestDto.getSurveyList().get(22));
        builder.q23(surveyRequestDto.getSurveyList().get(23));
        builder.q24(surveyRequestDto.getSurveyList().get(24));
        builder.q25(surveyRequestDto.getSurveyList().get(25));
        builder.q26(surveyRequestDto.getSurveyList().get(26));
        builder.q27(surveyRequestDto.getSurveyList().get(27));
        builder.q28(surveyRequestDto.getSurveyList().get(28));
        builder.q29(surveyRequestDto.getSurveyList().get(29));
        builder.q30(surveyRequestDto.getSurveyList().get(30));
        builder.q31(surveyRequestDto.getSurveyList().get(31));
        builder.q32(surveyRequestDto.getSurveyList().get(32));

        // 5. 빌드된 엔티티를 저장
        UserSurveyFlags userSurveyFlags = builder.build();
        userSurveyFlagsRepository.save(userSurveyFlags);
    }
}

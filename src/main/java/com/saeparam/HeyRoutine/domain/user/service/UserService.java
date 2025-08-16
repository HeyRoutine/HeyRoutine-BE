package com.saeparam.HeyRoutine.domain.user.service;


import com.saeparam.HeyRoutine.domain.user.dto.request.BankUserMakeRequestDto;
import com.saeparam.HeyRoutine.domain.user.dto.response.BankAccountResponseDto;
import com.saeparam.HeyRoutine.domain.user.dto.response.BankUserMakeResponseDto;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;
    private final WebClientBankUtil webClientBankUtil;

    /**
     * 회원가입시 해당유저의 계좌 생성
     */
    public void bankAccountMake(String email){
        String accountTypeUniqueNo = "001-1-027c9c26b2d247"; // 예시 계좌 상품 번호

        // 1. 첫 번째 API: 은행 서비스에 사용자 계정을 생성합니다.
        webClientBankUtil.makeUserAccount(email, BankUserMakeRequestDto.class, BankUserMakeResponseDto.class)
                .flatMap(userResponse -> {
                    // 2. 첫 번째 API 호출이 성공하면, 그 응답(userResponse)에서 userKey를 추출합니다.
                    String userKey = userResponse.getUserKey(); // getUserKey()는 응답 DTO에 있어야 합니다.
                    System.out.println("계정 생성 성공! UserKey: " + userKey);

                    // 3. 추출한 userKey를 사용하여 두 번째 API를 호출하고, 그 결과를 Mono로 반환합니다.
                    return webClientBankUtil.createDemandDepositAccount(
                            userKey,
                            accountTypeUniqueNo,
                            BankAccountResponseDto.class
                    );
                })
                .subscribe(
                        accountResponse -> {
                            // 4. 두 번째 API 호출까지 모두 성공하면 최종 결과를 받습니다.
                            System.out.println("요구불 계좌 생성 성공! 응답: " + accountResponse.toString());
                            // TODO: 최종 결과를 DB에 저장하는 등의 로직 수행
                        },
                        error -> {
                            // 5. 체인 중간에 어느 한 곳에서라도 에러가 발생하면 여기서 처리됩니다.
                            System.err.println("전체 프로세스 중 에러 발생: " + error.getMessage());
                        }
                );
    }

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
        bankAccountMake(signUpDto.getEmail());
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

    public String findByNickname(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        return user.getNickname();
    }

    public String checkEmailDuplicate(String email) {
        // 이메일 중복체크
        if (userRepository.existsByEmail(email)) {
            throw new UserHandler(ErrorStatus.USER_ID_IN_USE);
        }
        return "사용가능한 이메일입니다";

    }

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
    public String mypageResetPassword(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);
        return "비밀번호가 변경되었습니다";
    }

    /**
     * 닉네임 변경
     *
     * @param email
     * @param nickname
     * @return
     */

    @Transactional
    public String mypageResetNickname(String email, String nickname) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        if (userRepository.existsByNickname(nickname)) {
            throw new UserHandler(ErrorStatus.USER_NICKNAME_IN_USE);
        }
        user.setNickname(nickname);

        return "닉네임이 변경되었습니다";
    }

}

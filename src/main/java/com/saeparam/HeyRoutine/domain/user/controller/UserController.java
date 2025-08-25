package com.saeparam.HeyRoutine.domain.user.controller;



import com.saeparam.HeyRoutine.domain.user.dto.request.ResetPasswordDto;
import com.saeparam.HeyRoutine.global.web.response.ApiResponse;
import com.saeparam.HeyRoutine.domain.user.dto.JwtToken;
import com.saeparam.HeyRoutine.domain.user.dto.request.ReissueDto;
import com.saeparam.HeyRoutine.domain.user.dto.request.SignInDto;
import com.saeparam.HeyRoutine.domain.user.dto.request.SignUpDto;
import com.saeparam.HeyRoutine.domain.user.dto.response.UserDto;
import com.saeparam.HeyRoutine.domain.user.service.UserService;
import com.saeparam.HeyRoutine.global.security.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    private final JwtTokenProvider jwtTokenProvider;


    /**
     * 닉네임 보기
     */
    @PostMapping("/my-nickname")
    @Operation(summary = "내 닉네임 확인 API", description = "내 닉네임을 확인합니다.")

    public ResponseEntity<?> myNickname(@RequestHeader("Authorization") String token) {
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        return ResponseEntity.ok().body(ApiResponse.onSuccess(userService.findByNickname(userId)));
    }


    /**
     * 이메일 중복체크
     *
     * @param email
     * @return
     */
    @PostMapping("/email-duplicate-check")
    @Operation(summary = "이메일 중복확인 API", description = "이메일을 중복을 확인합니다.")

    public ResponseEntity<?> checkEmailDuplicate(@RequestParam("email") String email) {
        String result = userService.checkEmailDuplicate(email);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(result));
    }

    /**
     * 닉네임 중복체크
     *
     * @param nickname
     * @return
     */
    @PostMapping("/nickname-duplicate-check")
    @Operation(summary = "닉네임 중복확인 API", description = "닉네임 중복을 확인합니다.")
    public ResponseEntity<?> checknicknameDuplicate(@RequestParam("nickname") String nickname) {
        String result = userService.checknicknameDuplicate(nickname);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(result));
    }

    /**
     * 로그인
     *
     * @param signInDto
     * @return
     */
    @PostMapping("/sign-in")
    @Operation(summary = "로그인 API", description = "로그인을 합니다.")
    public ResponseEntity<?> signIn(@RequestBody SignInDto signInDto) {
        String email = signInDto.getEmail();
        String password = signInDto.getPassword();
        JwtToken jwtToken = userService.signIn(email, password);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(jwtToken));
    }

    /**
     * 회원가입
     *
     * @param signUpDto
     * @return
     */
    @PostMapping("/sign-up")
    @Operation(summary = "회원가입 API", description = "회원가입을 합니다.")
    public ResponseEntity<?> signUp(@RequestBody SignUpDto signUpDto) {
        // 회원가입 처리
        UserDto savedMemberDto = userService.signUp(signUpDto);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(savedMemberDto));
    }

    /**
     * 토큰 재발급
     *
     * @param reissueDto
     * @return
     */
    @PostMapping("/token/reissue")
    @Operation(summary = "토큰 재발급 API", description = "액세스토큰,리프레쉬토큰을 재발급합니다.(RTR)")
    public ResponseEntity<?> reissue(@RequestBody ReissueDto reissueDto) {
        // 토큰 재발급 처리
        JwtToken jwtToken = userService.reissue(reissueDto);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(jwtToken));
    }

    /**
     * mypage에서 비밀번호 재설정
     * @param token
     * @param password
     * @return
     */
    @PatchMapping("/mypage-password")
    @Operation(summary = "비밀번호 재설정 API", description = "비밀번호를 재설정합니다.")
    public ResponseEntity<?> mypageResetPassword(@RequestHeader("Authorization") String token,@RequestParam("password") String password) {
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        String result=userService.mypageResetPassword(userId,password);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(result));
    }

    /**
     * 비밀번호 찾기에서 비밀번호 설정
     */
    @PatchMapping("/password")
    @Operation(summary = "비밀번호 찾기 API", description = "비밀번호를 찾습니다.")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDto resetPasswordDto) {
        String result=userService.resetPassword(resetPasswordDto);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(result));
    }


    @PatchMapping("/mypage-nickname")
    @Operation(summary = "닉네임 재설정 API", description = "닉네임을 재설정합니다.")
    public ResponseEntity<?> resetNickname(@RequestHeader("Authorization") String token,@RequestParam("nickname") String nickname) {
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        String result=userService.mypageResetNickname(userId,nickname);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(result));
    }

    // 탈퇴



}

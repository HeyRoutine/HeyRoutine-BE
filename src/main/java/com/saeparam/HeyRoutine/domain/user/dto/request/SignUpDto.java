package com.saeparam.HeyRoutine.domain.user.dto.request;


import com.saeparam.HeyRoutine.domain.user.entity.User;
import com.saeparam.HeyRoutine.domain.user.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpDto {

    private String email; // 이메일
    private String password; // 비밀번호
    private String nickname; //닉네임
    private String profileImage;
    private List<Role> roles;

    public User toEntity(SignUpDto signUpDto,String encodedPassword) {

        return User.builder()
                .email(signUpDto.getEmail())
                .password(encodedPassword)
                .nickname(signUpDto.getNickname())
                .profileImage(signUpDto.getProfileImage())
                .roles(signUpDto.getRoles())
                .build();
    }
}
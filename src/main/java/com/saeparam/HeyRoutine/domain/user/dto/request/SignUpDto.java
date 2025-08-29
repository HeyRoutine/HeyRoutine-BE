package com.saeparam.HeyRoutine.domain.user.dto.request;

import com.saeparam.HeyRoutine.domain.user.entity.Major;
import com.saeparam.HeyRoutine.domain.user.entity.University;
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
    private Long universityId; // 대학교 ID
    private Long majorId;      // 학과 ID
    private Boolean isMarketing; // 마케팅 수신 동의 여부

    /**
     * Convert this DTO to a {@link User} entity.
     *
     * @param encodedPassword 인코딩된 비밀번호
     * @param university      사용자 대학교
     * @param major           사용자 학과
     * @return 생성된 {@link User}
     */
    public User toEntity(String encodedPassword, University university, Major major){
        return User.builder()
                .email(this.email)
                .password(encodedPassword)
                .nickname(this.nickname)
                .profileImage(this.profileImage)
                .point(0L)
                .roles(this.roles)
                .isMarketing(this.isMarketing)
                .university(university)
                .major(major)
                .build();
    }
}
package com.saeparam.HeyRoutine.user.dto.response;

import com.saeparam.HeyRoutine.user.domain.User;
import lombok.*;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

  private Long id;
  private String username;
  private String nickname;

  static public UserDto toDto(User user) {
    return UserDto.builder()
        .id(user.getId())
        .username(user.getEmail())
        .nickname(user.getNickname())
        .build();
  }

  public User toEntity() {
    return User.builder()
        .id(id)
        .email(username)
        .nickname(nickname)
        .build();
  }
}

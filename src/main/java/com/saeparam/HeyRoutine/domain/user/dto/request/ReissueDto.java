package com.saeparam.HeyRoutine.domain.user.dto.request;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReissueDto {
  private String accessToken;

  private String refreshToken;
}

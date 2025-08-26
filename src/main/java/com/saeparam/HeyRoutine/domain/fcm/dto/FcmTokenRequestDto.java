package com.saeparam.HeyRoutine.domain.fcm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class FcmTokenRequestDto {
    private String fcmToken;
}

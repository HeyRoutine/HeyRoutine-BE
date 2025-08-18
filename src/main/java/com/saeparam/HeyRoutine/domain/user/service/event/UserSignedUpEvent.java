package com.saeparam.HeyRoutine.domain.user.service.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserSignedUpEvent {
    private final String email;
}

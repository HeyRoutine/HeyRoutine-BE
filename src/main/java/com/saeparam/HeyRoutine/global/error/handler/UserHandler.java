package com.saeparam.HeyRoutine.global.error.handler;


import com.saeparam.HeyRoutine.global.error.exception.GeneralException;
import com.saeparam.HeyRoutine.global.web.response.code.BaseErrorCode;

public class UserHandler extends GeneralException {

    public UserHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}

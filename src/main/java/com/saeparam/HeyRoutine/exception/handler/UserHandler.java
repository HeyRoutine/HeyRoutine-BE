package com.saeparam.HeyRoutine.exception.handler;


import com.saeparam.HeyRoutine.exception.GeneralException;
import com.saeparam.HeyRoutine.response.code.BaseErrorCode;

public class UserHandler extends GeneralException {

    public UserHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}

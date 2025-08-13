package com.saeparam.HeyRoutine.global.error.handler;


import com.saeparam.HeyRoutine.global.error.exception.GeneralException;
import com.saeparam.HeyRoutine.global.web.response.code.BaseErrorCode;

public class TokenHandler extends GeneralException {
  public TokenHandler(BaseErrorCode errorCode) {
    super(errorCode);
  }
}

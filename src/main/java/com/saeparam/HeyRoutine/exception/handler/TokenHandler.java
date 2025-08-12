package com.saeparam.HeyRoutine.exception.handler;


import com.saeparam.HeyRoutine.exception.GeneralException;
import com.saeparam.HeyRoutine.response.code.BaseErrorCode;

public class TokenHandler extends GeneralException {
  public TokenHandler(BaseErrorCode errorCode) {
    super(errorCode);
  }
}

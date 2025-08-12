package com.saeparam.HeyRoutine.exception.handler;


import com.saeparam.HeyRoutine.exception.GeneralException;
import com.saeparam.HeyRoutine.response.code.BaseErrorCode;

public class MailHandler extends GeneralException {
  public MailHandler(BaseErrorCode errorCode) {
    super(errorCode);
  }
}

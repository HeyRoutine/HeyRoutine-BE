package com.saeparam.HeyRoutine.global.error.handler;


import com.saeparam.HeyRoutine.global.error.exception.GeneralException;
import com.saeparam.HeyRoutine.global.web.response.code.BaseErrorCode;

public class MailHandler extends GeneralException {
  public MailHandler(BaseErrorCode errorCode) {
    super(errorCode);
  }
}

package com.saeparam.HeyRoutine.global.error.exception;



import com.saeparam.HeyRoutine.global.web.response.code.BaseErrorCode;
import com.saeparam.HeyRoutine.global.web.response.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException {

    private BaseErrorCode code;

    public ErrorReasonDTO getErrorReason() {
        return this.code.getReason();
    }

    public ErrorReasonDTO getErrorReasonHttpStatus() {
        return this.code.getReasonHttpStatus();
    }
}

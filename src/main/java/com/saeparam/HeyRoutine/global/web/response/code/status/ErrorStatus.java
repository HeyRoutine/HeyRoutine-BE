package com.saeparam.HeyRoutine.global.web.response.code.status;


import com.saeparam.HeyRoutine.global.web.response.code.BaseErrorCode;
import com.saeparam.HeyRoutine.global.web.response.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // 일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),


    // Token 응답

    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN4001", "토큰이 만료되었습니다"),
    TOKEN_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "TOKEN4002", "잘못된 토큰 입니다."),
    UNSUPPORTED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN4003", "지원되지 않는 JWT 토큰입니다."),
    INVALID_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN4004", "JWT 토큰이 잘못되었습니다."),
    REFRESH_TOKEN_NOT_VALID(HttpStatus.UNAUTHORIZED, "TOKEN4005","Refresh Token 정보가 유효하지 않습니다."),
    REFRESH_TOKEN_NOT_MATCH(HttpStatus.UNAUTHORIZED, "TOKEN4005-1","Refresh Token 정보가 일치하지 않습니다."),
    TOKEN_IS_NOT_AUTHORITY(HttpStatus.UNAUTHORIZED,"TOKEN4006","권한 정보가 없는 토큰입니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN4007","Refresh Token이 만료되었습니다."),
    NO_AUTHENTICATION_INFORMATION(HttpStatus.UNAUTHORIZED,"TOKEN4008","인증 정보가 없는 토큰입니다."),

    // user 응답
    USER_ID_IN_USE(HttpStatus.BAD_REQUEST, "USER4000", "사용중인 이메일 입니다."),
    USER_ID_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER4004", "아이디를 잘못 입력했습니다"),
    USER_NICKNAME_IN_USE(HttpStatus.BAD_REQUEST, "USER4001", "사용중인 닉네임 입니다"),
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER4002", "해당 유저가 없습니다"),
    USER_NOT_AUTHORITY(HttpStatus.UNAUTHORIZED, "USER4002", "권한이 없습니다"),
    USER_INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST, "USER4003", "로그인 정보가 일치하지 않습니다."),
    USER_NOT_MATCH(HttpStatus.UNAUTHORIZED, "USER4004", "접근 권한이 없습니다."),

    // mail 응답
    MAIL_SEND_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "MAIL5000", "이메일 전송에 에러가 발생했습니다."),
    MAIL_NUMBER_IS_NULL(HttpStatus.BAD_REQUEST,"MAIL4000","인증번호를 입력해주세요"),
    MAIL_NUMBER_IS_NOT_MATCH(HttpStatus.BAD_REQUEST,"MAIL4000","인증번호가 틀렸습니다"),

    // shop 응답
    PRODUCT_NOT_FOUND(HttpStatus.BAD_REQUEST,"SHOP4000","해당 ID의 상품을 찾을 수 없습니다: "),
    STOCK_IS_NULL(HttpStatus.INTERNAL_SERVER_ERROR, "SHOP5000", "재고가 부족합니다"),
    USER_POINT_LACK(HttpStatus.INTERNAL_SERVER_ERROR, "SHOP5001", "유저의 포인트가 부족합니다"),

    // 루틴관련 응답
    MY_ROUTINE_LIST_NOT_FOUND(HttpStatus.BAD_REQUEST,"ROUTINE4000","해당 MyRoutineList 를 찾을 수 업습니다. "),

    EMOJI_NOT_FOUND(HttpStatus.NOT_FOUND,"ROUTINE4001","해당 이모지를 찾을 수 없습니다."),
    ROUTINE_TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "ROUTINE4002", "해당 루틴 템플릿이 존재하지 않습니다."),
    GROUP_ROUTINE_NOT_FOUND(HttpStatus.NOT_FOUND, "ROUTINE4003", "해당 단체루틴이 존재하지 않습니다."),
    SUB_ROUTINE_NOT_FOUND(HttpStatus.NOT_FOUND, "ROUTINE4004", "해당 상세루틴이 존재하지 않습니다."),
    GUESTBOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "ROUTINE4005", "해당 방명록이 존재하지 않습니다."),

    ALREADY_JOINED_ROUTINE(HttpStatus.CONFLICT, "ROUTINE4091", "이미 가입된 단체루틴입니다."),

    INVALID_TIME_FORMAT(HttpStatus.UNPROCESSABLE_ENTITY, "ROUTINE4221", "시간 형식이 올바르지 않습니다. HH:mm 형식으로 입력해주세요."),
    INVALID_ROUTINE_TYPE(HttpStatus.UNPROCESSABLE_ENTITY, "ROUTINE4222", "루틴 타입은 DAILY 또는 FINANCE만 가능합니다."),
    INVALID_DAY_OF_WEEK(HttpStatus.UNPROCESSABLE_ENTITY, "ROUTINE4223", "요일 데이터가 중복되거나 올바르지 않습니다."),
    INVALID_CATEGORY(HttpStatus.UNPROCESSABLE_ENTITY, "ROUTINE4224", "카테고리 형식이 올바르지 않습니다."),
    GROUP_ROUTINE_DETAIL_NOT_DONE(HttpStatus.UNPROCESSABLE_ENTITY, "ROUTINE4225", "단체루틴의 상세 루틴이 모두 성공 상태가 아닙니다."),
    GROUP_ROUTINE_DETAIL_ALREADY_DONE(HttpStatus.UNPROCESSABLE_ENTITY, "ROUTINE4226", "단체루틴의 상세 루틴이 이미 모두 성공 상태입니다."),

    GUESTBOOK_FORBIDDEN(HttpStatus.FORBIDDEN, "ROUTINE4031", "본인이 작성한 방명록만 수정/삭제할 수 있습니다."),
    GUESTBOOK_GET_FORBIDDEN(HttpStatus.FORBIDDEN, "ROUTINE4032", "해당 단체루틴에 속한 사용자만 볼 수 있습니다."),
    ROUTINE_FORBIDDEN(HttpStatus.FORBIDDEN, "ROUTINE4032", "루틴 관리자만 수정/삭제할 수 있습니다."),

    ROUTINE_NOT_FOUND(HttpStatus.BAD_REQUEST,"ROUTINE4001","해당 Routine 을 찾을 수 업습니다. ")

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build();
    }

}

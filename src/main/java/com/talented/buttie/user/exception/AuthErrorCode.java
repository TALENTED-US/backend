package com.talented.buttie.user.exception;

import com.talented.buttie.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    PASSWORD_NOT_VALID(HttpStatus.BAD_REQUEST, "AUTH_001", "비밀번호가 유효하지 않습니다."),
    PASSWORD_HAS_EMAIL(HttpStatus.BAD_REQUEST, "AUTH_002", "비밀번호에 이메일이 포함되어 있습니다."),
    PASSWORD_HAS_PHONENUMBER(HttpStatus.BAD_REQUEST, "AUTH_003", "비밀번호에 전화번호가 포함되어 있습니다."),
    PASSWORD_HAS_BIRTHDAY(HttpStatus.BAD_REQUEST, "AUTH_004", "비밀번호에 생일이 포함되어 있습니다."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "AUTH_005", "유효하지 않은 토큰입니다."),
    IDENTITY_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, "AUTH_006", "신원 확인에 실패했습니다. 다시 시도해 주십시오."),
    INVALID_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "AUTH_007", "유효하지 않은 전화번호입니다."),
    PASSWORD_UPDATE_FAILED(HttpStatus.BAD_REQUEST, "AUTH_008", "비밀번호 업데이트에 실패했습니다."),

    PASSWORD_NOT_MATCH(HttpStatus.UNAUTHORIZED, "AUTH_101", "비밀번호가 일치하지 않습니다."),
    REFRESH_TOKEN_SAVE_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_102", "로그인 토큰 저장에 실패했습니다."),
    REFRESH_TOKEN_DELETE_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_103", "로그인 토큰 삭제에 실패했습니다."),
    COOKIE_CREATE_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_104", "인증 쿠키 생성에 실패했습니다."),


    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_401", "아이디를 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_402", "사용자를 찾을 수 없습니다."),

    USER_CREATE_FAILED(HttpStatus.CONFLICT, "AUTH_901", "사용자 생성에 실패했습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_902", "이미 존재하는 이메일입니다."),
    PHONE_NUMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_903", "이미 존재하는 전화번호입니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_904", "이미 존재하는 닉네임입니다.");


    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}

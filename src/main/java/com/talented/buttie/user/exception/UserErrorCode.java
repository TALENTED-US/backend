package com.talented.buttie.user.exception;

import com.talented.buttie.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_401", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER_901", "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "USER_902", "이미 사용 중인 닉네임입니다."),
    EMPLOYMENT_PREPARATION_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_402", "취업 준비 정보를 찾을 수 없습니다."),
    EMPLOYMENT_PREPARATION_CREATE_FAILED(HttpStatus.BAD_REQUEST, "USER_001", "취업 준비 정보 등록에 실패했습니다."),
    PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "USER_101", "비밀번호가 일치하지 않습니다."),
    ;
    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}

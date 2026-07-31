package com.talented.buttie.user.exception;

import com.talented.buttie.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode  implements BaseErrorCode {

    PASSWORD_NOT_VALID(HttpStatus.BAD_REQUEST, "AUTH_001", "비밀번호가 유효하지 않습니다."),
    PASSWORD_HAS_USERNAME(HttpStatus.BAD_REQUEST, "AUTH_002", "비밀번호에 이름이 포함되어 있습니다."),
    PASSWORD_HAS_PHONENUMBER(HttpStatus.BAD_REQUEST, "AUTH_003", "비밀번호에 전화번호가 포함되어 있습니다."),
    PASSWORD_HAS_BIRTHDAY(HttpStatus.BAD_REQUEST, "AUTH_004", "비밀번호에 생일이 포함되어 있습니다."),
    USER_CREATE_FAILED(HttpStatus.CONFLICT, "AUTH_005", "사용자 생성에 실패했습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_006", "이미 존재하는 이메일입니다."),
    PHONE_NUMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_007", "이미 존재하는 전화번호입니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_008", "이미 존재하는 닉네임입니다."),
    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_009", "아이디를 찾을 수 없습니다."),
    PASSWORD_NOT_MATCH(HttpStatus.UNAUTHORIZED, "AUTH_010", "비밀번호가 일치하지 않습니다.");


    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}

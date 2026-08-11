package com.talented.buttie.mydata.exception;

import com.talented.buttie.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AccountErrorCode implements BaseErrorCode {

    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "ACCOUNT_001", "계좌 정보를 찾을 수 없습니다."),
    ACCOUNT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACCOUNT_002", "해당 계좌에 접근할 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}

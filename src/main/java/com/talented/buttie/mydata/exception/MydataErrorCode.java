package com.talented.buttie.mydata.exception;

import com.talented.buttie.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MydataErrorCode implements BaseErrorCode {

    MYDATA_CONNECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "MYDATA_001", "마이데이터 연결 정보를 찾을 수 없습니다."),
    MYDATA_CONNECTION_FAILED(HttpStatus.BAD_REQUEST, "MYDATA_002", "마이데이터 연결에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}


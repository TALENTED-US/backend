package com.talented.buttie.ledger.exception;

import com.talented.buttie.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum LedgerErrorCode implements BaseErrorCode {

    TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "LEDGER_001", "거래 정보를 찾을 수 없습니다."),

    TRANSACTION_BAD_REQUEST(HttpStatus.BAD_REQUEST, "LEDGER_002", "거래목록을 찾을 수 없습니다."),

    TRANSACTION_USER_ID_MISMATCH(HttpStatus.FORBIDDEN, "LEDGER_003", "다른 사용자의 거래에 대한 접근 권한이 없습니다.");
    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}


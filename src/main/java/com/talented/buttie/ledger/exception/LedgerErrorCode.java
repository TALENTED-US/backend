package com.talented.buttie.ledger.exception;

import com.talented.buttie.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum LedgerErrorCode implements BaseErrorCode {

    TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "LEDGER_001", "거래 정보를 찾을 수 없습니다."),
    CUSTOM_EXPENSE_NOT_FOUND(HttpStatus.BAD_REQUEST, "LEDGER_002", "사용자 설정 비용을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}


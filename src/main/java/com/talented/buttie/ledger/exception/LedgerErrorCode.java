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

    TRANSACTION_USER_ID_MISMATCH(HttpStatus.FORBIDDEN, "LEDGER_003", "다른 사용자의 거래를 수정할 권한이 없습니다."),
    TRANSACTION_MEMO_USER_ID_MISMATCH(HttpStatus.FORBIDDEN, "LEDGER_004", "다른 사용자의 거래 메모를 수정할 권한이 없습니다."),
    TRANSACTION_DELETE_USER_ID_MISMATCH(HttpStatus.FORBIDDEN, "LEDGER_005", "다른 사용자의 거래를 삭제할 권한이 없습니다."),
    EXTERNAL_TRANSACTION_UNMODIFIABLE(HttpStatus.FORBIDDEN, "LEDGER_008", "외부 기관에서 연동된 거래는 금액, 카테고리, 일시를 수정할 수 없습니다. (메모만 수정 가능)"),
    EXTERNAL_TRANSACTION_UNDELETABLE(HttpStatus.FORBIDDEN, "LEDGER_009", "외부 기관에서 연동된 거래는 삭제할 수 없습니다.");
    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}


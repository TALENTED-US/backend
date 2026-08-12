package com.talented.buttie.ledger.exception;

import com.talented.buttie.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum LedgerErrorCode implements BaseErrorCode {

    TRANSACTION_BAD_REQUEST(HttpStatus.BAD_REQUEST, "LEDGER_001", "거래목록을 찾을 수 없습니다."),
    ALREADY_FIXED_EXPENSE(HttpStatus.BAD_REQUEST, "LEDGER_002", "이미 고정 지출로 등록된 거래입니다."),
    ACCOUNT_TRANSFER_REQUIRED(HttpStatus.BAD_REQUEST, "LEDGER_003", "미분류 계좌이체 거래만 지출로 등록할 수 있습니다."),
    INVALID_EXPENSE_CLASSIFICATION_TYPE(HttpStatus.BAD_REQUEST, "LEDGER_004", "계좌이체는 일반 지출 또는 고정 지출로만 등록할 수 있습니다."),
    EXPENSE_CLASSIFICATION_REQUIRED(HttpStatus.BAD_REQUEST, "LEDGER_005", "지출로 분류된 거래만 고정 지출로 등록할 수 있습니다."),
    INVALID_MANUAL_TRANSACTION_TYPE(HttpStatus.BAD_REQUEST, "LEDGER_006", "수동 거래로 계좌이체를 등록할 수 없습니다."),

    TRANSACTION_USER_ID_MISMATCH(HttpStatus.FORBIDDEN, "LEDGER_301", "다른 사용자의 거래를 수정할 권한이 없습니다."),
    TRANSACTION_MEMO_USER_ID_MISMATCH(HttpStatus.FORBIDDEN, "LEDGER_302", "다른 사용자의 거래 메모를 수정할 권한이 없습니다."),
    TRANSACTION_DELETE_USER_ID_MISMATCH(HttpStatus.FORBIDDEN, "LEDGER_303", "다른 사용자의 거래를 삭제할 권한이 없습니다."),
    EXTERNAL_TRANSACTION_UNMODIFIABLE(HttpStatus.FORBIDDEN, "LEDGER_304", "외부 기관에서 연동된 거래는 금액, 카테고리, 일시를 수정할 수 없습니다. (메모만 수정 가능)"),
    EXTERNAL_TRANSACTION_UNDELETABLE(HttpStatus.FORBIDDEN, "LEDGER_305", "외부 기관에서 연동된 거래는 삭제할 수 없습니다."),

    TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "LEDGER_401", "거래 정보를 찾을 수 없습니다."),
    FIXED_EXPENSE_NOT_FOUND(HttpStatus.NOT_FOUND, "LEDGER_402", "등록된 고정 지출 내역을 찾을 수 없습니다."),
    NOT_FIXED_EXPENSE(HttpStatus.BAD_REQUEST, "LEDGER_403", "고정 지출로 등록된 거래가 아닙니다.");
    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}

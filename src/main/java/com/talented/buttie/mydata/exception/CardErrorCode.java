package com.talented.buttie.mydata.exception;

import com.talented.buttie.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CardErrorCode implements BaseErrorCode {

    CARD_NOT_FOUND(HttpStatus.NOT_FOUND, "CARD_001", "카드 정보를 찾을 수 없습니다."),
    CARD_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CARD_002", "카드 등록에 실패했습니다."),
    CARD_UPDATE_FAILED(HttpStatus.NOT_FOUND, "CARD_003", "수정할 카드 정보를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}

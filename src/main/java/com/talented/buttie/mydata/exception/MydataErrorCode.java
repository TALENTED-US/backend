package com.talented.buttie.mydata.exception;

import com.talented.buttie.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MydataErrorCode implements BaseErrorCode {

    MYDATA_CONNECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "MYDATA_001", "마이데이터 연결 정보를 찾을 수 없습니다."),
    MYDATA_CONNECTION_FAILED(HttpStatus.BAD_REQUEST, "MYDATA_002", "마이데이터 연결에 실패했습니다."),
    MYDATA_MOCK_API_FAILED(HttpStatus.BAD_GATEWAY, "MYDATA_003", "마이데이터 Mock 서버 조회에 실패했습니다."),
    MYDATA_AUTHORIZATION_FAILED(HttpStatus.BAD_GATEWAY, "MYDATA_004", "마이데이터 인가코드 발급에 실패했습니다."),
    MYDATA_TOKEN_ISSUE_FAILED(HttpStatus.BAD_GATEWAY, "MYDATA_005", "마이데이터 토큰 발급에 실패했습니다."),
    MYDATA_ALREADY_CONNECTED(HttpStatus.CONFLICT, "MYDATA_006", "이미 연결된 마이데이터입니다."),
    MYDATA_NOT_CONNECTED(HttpStatus.BAD_REQUEST, "MYDATA_007", "마이데이터 연결이 필요합니다."),
    MYDATA_ASSET_SELECTION_INVALID(HttpStatus.BAD_REQUEST, "MYDATA_008", "선택한 계좌 또는 카드를 찾을 수 없습니다."),
    MYDATA_TRANSACTION_SYNC_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MYDATA_009", "마이데이터 거래내역 저장에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}

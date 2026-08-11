package com.talented.buttie.catalog.exception;

import com.talented.buttie.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CatalogErrorCode implements BaseErrorCode {

    POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATALOG_001", "정책 정보를 찾을 수 없습니다."),
    FINANCE_PRODUCT_NOT_FOUND(HttpStatus.BAD_REQUEST, "CATALOG_002", "금융상품 정보를 찾을 수 없습니다."),

    POLICY_LIST_EMPTY(HttpStatus.NOT_FOUND, "CATALOG_003", "조회된 정책 목록이 없습니다."),
    INVALID_POLICY_DATA(HttpStatus.BAD_REQUEST, "CATALOG_004", "유효하지 않은 정책 데이터입니다."),
    POLICY_FETCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CATALOG_005", "정책 데이터를 가져오는 중 오류가 발생했습니다."),
    POLICY_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "CATALOG_006", "종료되었거나 신청 불가능한 정책입니다.");

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}

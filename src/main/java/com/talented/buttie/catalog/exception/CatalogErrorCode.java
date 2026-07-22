package com.talented.buttie.catalog.exception;

import com.talented.buttie.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CatalogErrorCode implements BaseErrorCode {

    POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATALOG_001", "정책 정보를 찾을 수 없습니다."),
    FINANCE_PRODUCT_NOT_FOUND(HttpStatus.BAD_REQUEST, "CATALOG_002", "금융상품 정보를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}


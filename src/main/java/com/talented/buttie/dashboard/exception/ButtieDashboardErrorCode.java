package com.talented.buttie.dashboard.exception;

import com.talented.buttie.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ButtieDashboardErrorCode implements BaseErrorCode {

    BUTTIE_DASHBOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "BUTTIE_DASHBOARD_401", "버티 대시보드 정보를 조회할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}

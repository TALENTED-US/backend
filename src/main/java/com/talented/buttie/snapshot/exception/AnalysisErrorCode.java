package com.talented.buttie.snapshot.exception;

import com.talented.buttie.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AnalysisErrorCode implements BaseErrorCode {

    SNAPSHOT_NOT_FOUND(HttpStatus.NOT_FOUND, "ANALYSIS_401", "재정 스냅샷을 찾을 수 없습니다."),
    ANALYSIS_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "ANALYSIS_001", "재정 분석을 수행할 수 없습니다."),
    SNAPSHOT_OWNER_MISMATCH(HttpStatus.CONFLICT, "ANALYSIS_901", "스냅샷 소유자 정보가 일치하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}


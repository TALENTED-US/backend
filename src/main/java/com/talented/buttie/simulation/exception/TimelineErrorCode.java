package com.talented.buttie.simulation.exception;

import com.talented.buttie.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TimelineErrorCode implements BaseErrorCode {

    TIMELINE_NOT_FOUND(HttpStatus.NOT_FOUND, "TIMELINE_401", "타임라인 정보를 조회할 수 없습니다."),
    SNAPSHOT_NOT_FOUND(HttpStatus.NOT_FOUND, "TIMELINE_402", "스냅샷 정보를 찾을 수 없습니다."),
    SIMULATION_NOT_FOUND(HttpStatus.NOT_FOUND, "TIMELINE_403", "시뮬레이션 정보를 찾을 수 없습니다."),
    EMPLOYMENT_PREPARATION_NOT_FOUND(HttpStatus.NOT_FOUND, "TIMELINE_404", "목표 취업일 및 위험잔액 정보를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}

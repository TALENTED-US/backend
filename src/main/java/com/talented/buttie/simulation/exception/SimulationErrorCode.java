package com.talented.buttie.simulation.exception;

import com.talented.buttie.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SimulationErrorCode implements BaseErrorCode {

    SIMULATION_NOT_FOUND(HttpStatus.NOT_FOUND, "SIMULATION_401", "시뮬레이션을 찾을 수 없습니다."),
    SIMULATION_ITEM_NOT_FOUND(HttpStatus.BAD_REQUEST, "SIMULATION_001", "시뮬레이션 항목을 찾을 수 없습니다."),
    SIMULATION_PROJECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "SIMULATION_402", "예상 재정 계획을 찾을 수 없습니다."),
    CONFIRMED_SIMULATION_NOT_FOUND(HttpStatus.NOT_FOUND, "SIMULATION_403", "확정 시뮬레이션을 찾을 수 없습니다."),
    INVALID_SIMULATION_PERIOD(HttpStatus.BAD_REQUEST, "SIMULATION_002", "시뮬레이션 수행 기간이 올바르지 않습니다."),
    CONFIRMED_SIMULATION_CANNOT_BE_UPDATED(HttpStatus.CONFLICT, "SIMULATION_901", "확정 재정 계획은 기간을 수정할 수 없습니다."),
    INVALID_SIMULATION_ITEM(HttpStatus.BAD_REQUEST, "SIMULATION_004", "시뮬레이션 항목 값이 올바르지 않습니다.");

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}

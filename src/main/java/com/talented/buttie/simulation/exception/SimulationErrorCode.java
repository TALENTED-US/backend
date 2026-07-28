package com.talented.buttie.simulation.exception;

import com.talented.buttie.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SimulationErrorCode implements BaseErrorCode {

    SIMULATION_NOT_FOUND(HttpStatus.NOT_FOUND, "SIMULATION_001", "시뮬레이션을 찾을 수 없습니다."),
    SIMULATION_ITEM_NOT_FOUND(HttpStatus.BAD_REQUEST, "SIMULATION_002", "시뮬레이션 항목을 찾을 수 없습니다."),
    SIMULATION_PROJECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "SIMULATION_003", "예상 재정 계획을 찾을 수 없습니다."),
    CONFIRMED_SIMULATION_NOT_FOUND(HttpStatus.NOT_FOUND, "SIMULATION_004", "확정 시뮬레이션을 찾을 수 없습니다.");
    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}

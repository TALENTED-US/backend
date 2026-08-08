package com.talented.buttie.simulation.exception;

import com.talented.buttie.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SimulationErrorCode implements BaseErrorCode {

    NOT_CONFIRMED_SIMULATION_NOT_FOUND(HttpStatus.NOT_FOUND, "SIMULATION_401", "미확정 시뮬레이션을 찾을 수 없습니다."),
    CONFIRMED_SIMULATION_NOT_FOUND(HttpStatus.NOT_FOUND, "SIMULATION_402", "확정 시뮬레이션을 찾을 수 없습니다."),
    SIMULATION_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "SIMULATION_403", "시뮬레이션 항목을 찾을 수 없습니다."),
    SIMULATION_PROJECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "SIMULATION_404", "예상 재정 계획을 찾을 수 없습니다."),

    INVALID_SIMULATION_PERIOD(HttpStatus.BAD_REQUEST, "SIMULATION_001", "시뮬레이션 수행 기간이 올바르지 않습니다."),
    INVALID_SIMULATION_ITEM(HttpStatus.BAD_REQUEST, "SIMULATION_002", "시뮬레이션 항목 값이 올바르지 않습니다."),
    POLICY_ITEM_CANNOT_BE_UPDATED(HttpStatus.BAD_REQUEST, "SIMULATION_003", "정책 항목은 수정 불가합니다."),
    INVALID_INCOME_ITEM_NAME(HttpStatus.BAD_REQUEST, "SIMULATION_004", "수입 항목 이름을 확인해주세요."),
    ITEM_NAME_NOT_ALLOWED_FOR_EXPENSE(HttpStatus.BAD_REQUEST, "SIMULATION_005", "지출 항목의 이름은 수정 불가합니다."),
    INVALID_SIMULATION_ITEM_APPLY_PERIOD(HttpStatus.BAD_REQUEST, "SIMULATION_006", "항목 적용 기간이 올바르지 않습니다."),

    ALREADY_NOT_CONFIRMED_SIMULATION_EXISTS(HttpStatus.CONFLICT, "SIMULATION_901", "이미 미확정 시뮬레이션이 존재합니다."),
    ALREADY_CONFIRMED_SIMULATION_EXISTS(HttpStatus.CONFLICT, "SIMULATION_902", "이미 확정 시뮬레이션이 존재합니다."),
    CONFIRMED_SIMULATION_CANNOT_BE_UPDATED(HttpStatus.CONFLICT, "SIMULATION_903", "확정 재정 계획은 수정 불가합니다.");


    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}

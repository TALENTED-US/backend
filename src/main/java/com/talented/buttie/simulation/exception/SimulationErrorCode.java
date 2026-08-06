package com.talented.buttie.simulation.exception;

import com.talented.buttie.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SimulationErrorCode implements BaseErrorCode {

    SIMULATION_NOT_FOUND(HttpStatus.NOT_FOUND, "SIMULATION_401", "시뮬레이션을 찾을 수 없습니다."),
    SIMULATION_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "SIMULATION_402", "시뮬레이션 항목을 찾을 수 없습니다."),
    SIMULATION_PROJECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "SIMULATION_403", "예상 재정 계획을 찾을 수 없습니다."),
    CONFIRMED_SIMULATION_NOT_FOUND(HttpStatus.NOT_FOUND, "SIMULATION_404", "확정 시뮬레이션을 찾을 수 없습니다."),
    INVALID_SIMULATION_PERIOD(HttpStatus.BAD_REQUEST, "SIMULATION_001", "시뮬레이션 수행 기간이 올바르지 않습니다."),
    CONFIRMED_SIMULATION_CANNOT_BE_UPDATED(HttpStatus.CONFLICT, "SIMULATION_901", "확정 재정 계획은 수정 불가합니다."),
    INVALID_SIMULATION_ITEM(HttpStatus.BAD_REQUEST, "SIMULATION_002", "시뮬레이션 항목 값이 올바르지 않습니다."),

    EMPTY_SIMULATION_ITEM_UPDATE(HttpStatus.BAD_REQUEST, "SIMULATION_003", "수정할 항목 값을 입력해주세요."),
    POLICY_ITEM_CANNOT_BE_UPDATED(HttpStatus.BAD_REQUEST, "SIMULATION_004", "정책 항목은 수정 불가합니다."),
    INVALID_INCOME_ITEM_NAME(HttpStatus.BAD_REQUEST, "SIMULATION_005", "수입 항목 이름을 확인해주세요."),
    ITEM_NAME_NOT_ALLOWED_FOR_EXPENSE(HttpStatus.BAD_REQUEST, "SIMULATION_006", "지출 항목의 이름은 수정 불가합니다."),
    INVALID_SIMULATION_ITEM_APPLY_PERIOD(HttpStatus.BAD_REQUEST, "SIMULATION_007", "항목 적용 기간이 올바르지 않습니다.");

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}

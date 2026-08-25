package com.talented.buttie.quest.exception;

import com.talented.buttie.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum QuestErrorCode implements BaseErrorCode {

    QUEST_USER_ID_MISMATCH(HttpStatus.FORBIDDEN, "QUEST_301", "해당 퀘스트에 대한 접근 권한이 없습니다."),
    QUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "QUEST_401", "퀘스트를 찾을 수 없습니다."),

    APPLIED_SIMULATION_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "QUEST_405", "적용된 시뮬레이션 항목을 찾을 수 없습니다."),

    INVALID_SIMULATION_ID(HttpStatus.BAD_REQUEST, "QUEST_001", "가져온 시뮬레이션 ID가 올바르지 않거나 일치하지 않습니다."),
    SIMULATION_NOT_APPLIED(HttpStatus.BAD_REQUEST, "QUEST_002", "적용(확정)되지 않은 시뮬레이션은 퀘스트로 생성할 수 없습니다."),
    INVALID_SIMULATION_ITEM_DATA(HttpStatus.BAD_REQUEST, "QUEST_003", "시뮬레이션 항목 데이터가 유효하지 않아 퀘스트를 생성할 수 없습니다."),
    QUEST_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "QUEST_004", "완료되지 않은 퀘스트는 취소(진행중으로 변경)할 수 없습니다."),
    QUEST_EXP_REVERT_FAILED(HttpStatus.BAD_REQUEST, "QUEST_005", "퀘스트 경험치 회수에 실패했습니다."),
    QUEST_STATUS_UPDATE_FAILED(HttpStatus.BAD_REQUEST, "QUEST_006", "퀘스트 상태 변경에 실패했습니다."),
    QUEST_EXP_GRANT_FAILED(HttpStatus.BAD_REQUEST, "QUEST_007", "퀘스트 경험치 지급에 실패했습니다."),

    QUEST_ALREADY_COMPLETED(HttpStatus.CONFLICT, "QUEST_901", "이미 완료된 퀘스트입니다."),
    QUEST_ALREADY_GENERATED(HttpStatus.CONFLICT, "QUEST_902", "해당 시뮬레이션 항목에 대한 퀘스트 목록이 이미 생성되었습니다.");


    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}

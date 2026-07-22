package com.talented.buttie.quest.exception;

import com.talented.buttie.common.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum QuestErrorCode implements BaseErrorCode {

    QUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "QUEST_001", "퀘스트를 찾을 수 없습니다."),
    QUEST_ALREADY_COMPLETED(HttpStatus.CONFLICT, "QUEST_002", "이미 완료된 퀘스트입니다.");

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}

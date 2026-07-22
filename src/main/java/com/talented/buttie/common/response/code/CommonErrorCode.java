package com.talented.buttie.common.response.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommonErrorCode implements BaseErrorCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_001", "잘못된 요청입니다."),
    METHOD_ARGUMENT_NOT_VALID(HttpStatus.BAD_REQUEST, "COMMON_002", "올바르지 않은 요청 형식입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_003", "인증 과정에서 오류가 발생했습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_004", "금지된 요청입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_005", "지원하지 않는 Http Method 입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_006", "서버 에러가 발생했습니다."),
    ID_MISMATCH(HttpStatus.BAD_REQUEST, "COMMON_007", "요청 ID가 URL의 ID와 일치하지 않습니다."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "COMMON_008", "지원되지 않는 파일 형식입니다."),
    NOT_SUPPORTED_MEDIA_TYPE_ERROR(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "COMMON_009", "잘못된 미디어 타입입니다."),
    EXCEED_MAX_FILE_SIZE(HttpStatus.BAD_REQUEST, "COMMON_010", "파일이 최대 용량을 초과했습니다. (최대 용량 : 10MB)"),
    FILE_NOT_UPLOAD(HttpStatus.BAD_REQUEST, "COMMON_011", "업로드할 파일을 선택해주세요."),
    INVALID_EXCEL_EXTENSION(HttpStatus.BAD_REQUEST, "COMMON_012", "지원되지 않는 파일 형식입니다. (허용된 형식 : xls, xlsx)"),
    INVALID_IMAGE_EXTENSION(HttpStatus.BAD_REQUEST, "COMMON_013", "지원되지 않는 파일 형식입니다. (허용된 형식 : jpg, jpeg, png)");

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}

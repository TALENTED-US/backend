package com.talented.buttie.ledger.dto.request;

import com.talented.buttie.ledger.domain.ExpenseCategory;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@ApiModel(description = "수동 거래 목록 수정 요청 (외부거래 ID가 없을 때 사용)")
@Builder
public record UpdateTransactionRequestDTO(
    @ApiModelProperty(value = "거래 금액", example = "10000", required = true)
    @NotNull(message = "거래 금액 입력은 필수입니다.")
    @Positive(message = "금액은 0보다 커야합니다.")
    Integer transactionAmount,

    @ApiModelProperty(value = "카테고리 (FOOD, TRANSPORT, HOUSING, COMMUNICATION, SUBSCRIPTION, EDUCATION, CERTIFICATE, ETC_EXPENSE)", example = "FOOD", required = true)
    @NotNull(message = "지출 카테고리 선택은 필수입니다.")
    ExpenseCategory expenseCategory,

    @ApiModelProperty(value = "거래일시 (yyyy-MM-ddTHH:mm:ss)", example = "2026-07-28T00:00:00", required = true)
    @NotNull(message = "거래 일시는 필수입니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime transactionDate,

    @ApiModelProperty(value = "메모", example = "학식당에서 스팸치즈순두부찌개", required = false)
    @Size(max = 255, message = "메모는 최대 255자까지 입력 가능합니다.")
    String transactionMemo
) {

}

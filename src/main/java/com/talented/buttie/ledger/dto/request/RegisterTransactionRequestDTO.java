package com.talented.buttie.ledger.dto.request;

import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.ledger.domain.TransactionType;
import com.talented.buttie.ledger.domain.TransactionVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import lombok.Builder;

@ApiModel(description = "거래 목록 수동 추가 요청")
@Builder
public record RegisterTransactionRequestDTO(
    @ApiModelProperty(
        value = "거래 내용",
        example = "세종대학교 학식당",
        required = true
    )
    @NotBlank(message = "거래 내용은 필수입니다.")
    String transactionContent,

    @ApiModelProperty(
        value = "지출 유형 (EXPENSE, INCOME, FIXED)",
        example = "EXPENSE",
        required = true
    )
    @NotNull(message = "수입인지 지출인지 구분해야 합니다.")
    TransactionType type,

    @ApiModelProperty(
        value = "거래 금액",
        example = "10000",
        required = true
    )
    @NotNull(message = "거래 금액 입력은 필수입니다.")
    @Positive(message = "금액은 0보다 커야합니다.")
    Integer amount,

    @ApiModelProperty(
        value = "카테고리 (FOOD, TRANSPORT, HOUSING, COMMUNICATION, SUBSCRIPTION, EDUCATION, CERTIFICATE, ETC_EXPENSE)",
        example = "FOOD",
        required = true
    )
    @NotNull(message = "지출 카테고리 선택은 필수입니다.")
    ExpenseCategory category,

    @ApiModelProperty(
        value = "거래일시 (yyyy-MM-ddTHH:mm:ss)",
        example = "2026-07-28T00:00:00",
        required = true
    )
    @NotBlank(message = "거래 일시는 필수입니다.")
    String transactionDate,

    @ApiModelProperty(
        value = "메모",
        example = "학식당에서 스팸치즈순두부찌개",
        required = false
    )
    @Size(max = 255, message = "메모는 최대 255자까지 입력 가능합니다.")
    String memo
) {
    public TransactionVO toVO(Long userId){
        return TransactionVO.builder()
            .userId(userId)
            .transactionContent(transactionContent)
            .transactionType(this.type)
            .transactionAmount(this.amount)
            .expenseCategory(this.category)
            .transactionAt(LocalDateTime.parse(transactionDate.replace(" ", "T")))
            .transactionMemo(this.memo)
            .build();
    }
}

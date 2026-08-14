package com.talented.buttie.ledger.dto.request.transaction;

import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.ledger.domain.TransactionType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Builder;

@ApiModel(description = "계좌이체 거래를 지출로 등록하는 요청")
@Builder
public record ClassifyAccountTransactionRequest(
    @ApiModelProperty(value = "지출 유형", example = "EXPENSE 또는 FIXED", required = true)
    @NotNull(message = "지출 유형 선택은 필수입니다.")
    TransactionType transactionType,

    @ApiModelProperty(value = "지출 카테고리", example = "HOUSING_COMMUNICATION", required = true)
    @NotNull(message = "지출 카테고리 선택은 필수입니다.")
    ExpenseCategory expenseCategory
) {
}

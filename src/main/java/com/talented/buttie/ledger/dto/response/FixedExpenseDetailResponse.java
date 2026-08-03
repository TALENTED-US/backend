package com.talented.buttie.ledger.dto.response;

import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.ledger.domain.TransactionVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Builder;

@ApiModel(description = "고정 지출 상세 조회 응답")
@Builder
public record FixedExpenseDetailResponse(
    @ApiModelProperty(value = "거래 내용", example = "월세")
    @NotBlank (message = "거래 내용은 필수입니다.")
    String transactionContent,

    @ApiModelProperty(value = "거래 금액", example = "500000")
    @NotNull
    Integer transactionAmount,

    @ApiModelProperty(value = "지출 카테고리", example = "HOUSING")
    @NotNull
    ExpenseCategory expenseCategory
) {

    public static FixedExpenseDetailResponse from(TransactionVO vo) {
        return FixedExpenseDetailResponse.builder()
            .transactionContent(vo.getTransactionContent())
            .transactionAmount(vo.getTransactionAmount())
            .expenseCategory(vo.getExpenseCategory())
            .build();
    }
}

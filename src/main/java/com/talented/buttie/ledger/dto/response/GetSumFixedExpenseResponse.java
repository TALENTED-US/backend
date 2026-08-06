package com.talented.buttie.ledger.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Builder;

@ApiModel(description = "월별 고정 지출 금액 합계 응답")
@Builder
public record GetSumFixedExpenseResponse(
    @ApiModelProperty(value = "지난달 고정 지출 총 금액", example = "500000")
    @NotNull
    Integer lastMonthTotalFixedExpenseAmount,

    @ApiModelProperty(value = "이번달 고정 지출 총 금액", example = "560000")
    @NotNull
    Integer currentMonthTotalFixedExpenseAmount
) {
    public static GetSumFixedExpenseResponse of(
        Integer lastMonthTotalFixedExpenseAmount,
        Integer currentMonthTotalFixedExpenseAmount
    ) {
        return GetSumFixedExpenseResponse.builder()
            .lastMonthTotalFixedExpenseAmount(lastMonthTotalFixedExpenseAmount)
            .currentMonthTotalFixedExpenseAmount(currentMonthTotalFixedExpenseAmount)
            .build();
    }
}

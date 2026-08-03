package com.talented.buttie.simulation.dto.response;

import com.talented.buttie.simulation.domain.SimulationItemCategory;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

@ApiModel("시뮬레이션 항목 대입 미리보기 응답")
@Builder
public record PreviewItemResponse(
    @ApiModelProperty(value = "월별 적용 전/후 잔액 목록")
    List<MonthlyBalancePreview> monthlyBalances,

    @ApiModelProperty(value = "월별 현금흐름 적용 전/후 비교")
    CashFlowPreview cashflow,

    @ApiModelProperty(value = "미리보기 항목 효과 요약")
    ItemEffectPreview itemEffect
) {
    @ApiModel("월별 잔액 미리보기")
    @Builder
    public record MonthlyBalancePreview(
        @ApiModelProperty(value = "예측 기준 월", example = "2026-08-01")
        LocalDate projectionMonth,

        @ApiModelProperty(value = "적용 전 월말 예상 잔액", example = "4000000")
        Integer beforeClosingBalance,

        @ApiModelProperty(value = "적용 후 월말 예상 잔액", example = "4050000")
        Integer afterClosingBalance,

        @ApiModelProperty(value = "적용 전/후 잔액 차이", example = "50000")
        Integer balanceDelta
    ) {}

    @ApiModel("현금흐름 미리보기")
    @Builder
    public record CashFlowPreview(
       @ApiModelProperty(value = "적용 전 월수입", example = "1000000")
       Integer beforeMonthlyIncome,

       @ApiModelProperty(value = "적용 후 월수입", example = "1300000")
       Integer afterMonthlyIncome,

       @ApiModelProperty(value = "월수입 변화량", example = "300000")
       Integer incomeDelta,

       @ApiModelProperty(value = "적용 전 월지출", example = "2000000")
       Integer beforeMonthlyExpense,

       @ApiModelProperty(value = "적용 후 월지출", example = "1950000")
       Integer afterMonthlyExpense,

       @ApiModelProperty(value = "월지출 변화량", example = "-50000")
       Integer expenseDelta,

       @ApiModelProperty(value = "적용 전 월 순현금흐름", example = "-1000000")
       Integer beforeMonthlyNetCashFlow,

       @ApiModelProperty(value = "적용 후 월 순현금흐름", example = "-950000")
       Integer afterMonthlyNetCashFlow,

       @ApiModelProperty(value = "월 순현금흐름 변화량", example = "50000")
       Integer netCashFlowDelta
    ) {}

    @ApiModel("항목 효과 미리보기")
    @Builder
    public record ItemEffectPreview(
        @ApiModelProperty(value = "항목 이름", example = "식비 절약")
        String itemName,

        @ApiModelProperty(value = "항목 카테고리", example = "EXPENSE")
        SimulationItemCategory category,

        @ApiModelProperty(value = "매월 반영되는 효과 금액", example = "50000")
        Integer monthlyEffectAmount,

        @ApiModelProperty(value = "일회성으로 반영되는 효과 금액", example = "0")
        Integer onceEffectAmount
    ) {}
}

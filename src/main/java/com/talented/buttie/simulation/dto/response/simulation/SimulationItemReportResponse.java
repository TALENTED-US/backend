package com.talented.buttie.simulation.dto.response.simulation;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

@ApiModel("시뮬레이션 적용 항목 결과 보고서 응답")
@Builder
public record SimulationItemReportResponse(
    @ApiModelProperty(value = "현재 버티는 기간")
    BigDecimal currentPrepMonths,

    @ApiModelProperty(value = "예상 버티는 기간")
    BigDecimal expectPrepMonths,

    @ApiModelProperty(value = "현재 현금흐름 유지 시 자금이 고갈되지 않는지 여부")
    Boolean currentSustainable,

    @ApiModelProperty(value = "시뮬레이션 적용 후 자금이 고갈되지 않는지 여부")
    Boolean expectSustainable,

    @ApiModelProperty(value = "월별 재정 타임라인")
    List<MonthlyBalanceReport> monthlyBalances,

    @ApiModelProperty(value = "월별 현금흐름 적용 전/후 비교")
    CashFlowReport cashflow,

    @ApiModelProperty(value = "카테고리별 기여 금액 소계")
    CategoryContributionReport categoryContribution
) {

    @ApiModel("월별 재정 타임라인")
    @Builder
    public record MonthlyBalanceReport(
        @ApiModelProperty(value = "예측 기준 월", example = "2026-08-01")
        LocalDate projectionMonth,

        @ApiModelProperty(value = "적용 전 월말 예상 잔액", example = "4000000")
        Integer beforeClosingBalance,

        @ApiModelProperty(value = "적용 후 월말 예상 잔액", example = "4050000")
        Integer afterClosingBalance,

        @ApiModelProperty(value = "생활자금 최소 기준", example = "500000")
        Integer livingFundThreshold,

        @ApiModelProperty(value = "적용 후 예상 잔액이 생활자금 최소 기준보다 낮은지 여부")
        Boolean belowLivingFundThreshold
    ) {

    }

    @ApiModel("현금흐름 결과 보고서")
    @Builder
    public record CashFlowReport(
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
    ) {

    }

    @ApiModel("카테고리별 기여 금액")
    @Builder
    public record CategoryContributionReport(
        @ApiModelProperty(value = "지출 절감 월 반복 기여 금액", example = "50000")
        Integer expenseMonthlyAmount,

        @ApiModelProperty(value = "지출 절감 일회성 기여 금액", example = "0")
        Integer expenseOnceAmount,

        @ApiModelProperty(value = "수입 증가 월 반복 기여 금액", example = "300000")
        Integer incomeMonthlyAmount,

        @ApiModelProperty(value = "수입 증가 일회성 기여 금액", example = "0")
        Integer incomeOnceAmount,

        @ApiModelProperty(value = "정책 지원 월 반복 기여 금액", example = "200000")
        Integer policyMonthlyAmount,

        @ApiModelProperty(value = "정책 지원 일회성 기여 금액", example = "0")
        Integer policyOnceAmount
    ) {

    }
}

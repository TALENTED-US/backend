package com.talented.buttie.simulation.dto.response;

import com.talented.buttie.simulation.domain.MonthlyProjectionVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import lombok.Builder;

@Builder
@ApiModel(description = "월별 재정 계획 응답")
public record MonthlyProjectionResponse(

    @ApiModelProperty(value = "월별 재정 계획 ID", example = "1")
    Long projectionId,

    @ApiModelProperty(value = "예측 기준 월", example = "2026-08-01")
    LocalDate projectionMonth,

    @ApiModelProperty(value = "월초 예상 잔액", example = "5000000")
    Integer openingBalance,

    @ApiModelProperty(value = "예상 수입", example = "2500000")
    Integer expectedIncome,

    @ApiModelProperty(value = "예상 지출", example = "18000000")
    Integer expectedExpense,

    @ApiModelProperty(value = "월말 예상 잔액", example = "5700000")
    Integer closingBalance,

    @ApiModelProperty(value = "재정 조정 필요 여부", example = "false")
    Boolean adjustmentRequired,

    @ApiModelProperty(value = "재정 조정 필요 사유", example = "예상 지출이 예상 수입을 초과합니다.")
    String adjustmentReason
) {
    public static MonthlyProjectionResponse from(MonthlyProjectionVO monthlyProjection){
        return MonthlyProjectionResponse.builder()
            .projectionId(monthlyProjection.getProjectionId())
            .projectionMonth(monthlyProjection.getProjectionMonth())
            .openingBalance(monthlyProjection.getOpeningBalance())
            .expectedIncome(monthlyProjection.getExpectedIncome())
            .expectedExpense(monthlyProjection.getExpectedExpense())
            .closingBalance(monthlyProjection.getClosingBalance())
            .adjustmentRequired(monthlyProjection.getAdjustmentRequired())
            .adjustmentReason(monthlyProjection.getAdjustmentReason())
            .build();
    }
}

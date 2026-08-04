package com.talented.buttie.simulation.dto.response;

import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.ledger.domain.RecurrenceType;
import com.talented.buttie.simulation.domain.SimulationItemCategory;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record SimulationItemResponse(

    @ApiModelProperty(value = "시뮬레이션 항목 ID", example = "1")
    Long itemId,

    @ApiModelProperty(value = "시뮬레이션 항목 카테고리", example = "EXPENSE")
    SimulationItemCategory itemCategory,

    @ApiModelProperty(value = "시뮬레이션 항목 이름", example = "식비 줄이기")
    String displayName,

    @ApiModelProperty(value = "지출 항목 카테고리(지출 카테고리만 해당)", example = "FOOD")
    ExpenseCategory expenseCategory,

    @ApiModelProperty(value = "항목 적용 금액", example = "100000")
    int amount,

    @ApiModelProperty(value = "항목 적용 시작알", example = "2026-09-01")
    LocalDate applyStartDate,

    @ApiModelProperty(value = "항목 적용 종료일", example = "2026-12-01")
    LocalDate applyEndDate,

    @ApiModelProperty(value = "반복 여부", example = "ONCE")
    RecurrenceType recurrenceType,

    @ApiModelProperty(value = "항목 정책 ID. 정책 카테고리만 해당", example = "1")
    Long policyId,

    @ApiModelProperty(value = "항목 적용 정책 이름. 정책 카테고리만 해당", example = "청년월세지원금")
    String policyName
) {
}

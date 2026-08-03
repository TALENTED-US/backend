package com.talented.buttie.simulation.dto.request;

import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.simulation.domain.SimulationItemCategory;
import com.talented.buttie.simulation.domain.SimulationRecurrenceType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import lombok.Builder;

@ApiModel("시뮬레이션 항목 적용 요청")
@Builder
public record ApplySimulationItemRequest(
    @ApiModelProperty(value = "시뮬레이션 항목 카테고리", example = "EXPENSE", required = true)
    @NotNull(message = "시뮬레이션 항목 카테고리는 필수입니다.")
    SimulationItemCategory category,

    @ApiModelProperty(value = "수입 항목 이름. 수입 항목일 때 필수입니다.", example = "정기 알바")
    String itemName,

    @ApiModelProperty(value = "지출 줄이기 항목 카테고리. 지출 항목일 때 필수입니다.", example = "FOOD")
    ExpenseCategory expenseCategory,

    @ApiModelProperty(value = "항목 적용 금액. 정책 항목은 서버에서 policyId로 조회한 금액을 사용합니다.", example = "100000")
    @PositiveOrZero(message = "항목 적용 금액은 0 이상이어야 합니다.")
    Integer amount,

    @ApiModelProperty(value = "정책 ID. 정책 항목일 때 필수입니다.", example = "1")
    Long policyId,

    @ApiModelProperty(value = "항목 적용 시작일", example = "2026-09-01", required = true)
    @NotNull(message = "항목 적용 시작일은 필수입니다.")
    LocalDate applyStartDate,

    @ApiModelProperty(value = "항목 적용 종료일. 일회성 항목은 생략할 수 있습니다.", example = "2027-01-01")
    LocalDate applyEndDate,

    @ApiModelProperty(value = "반복 유형. 수입/지출 항목일 때 필수입니다. 정책 항목은 서버에서 정책 정보 기준으로 결정합니다.", example = "MONTHLY")
    SimulationRecurrenceType recurrenceType,

    @ApiModelProperty(value = "항목 상세 조건 값", example = "식비")
    String detailValue
) {
}

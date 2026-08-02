package com.talented.buttie.simulation.dto.request;

import com.talented.buttie.simulation.domain.SimulationItemCategory;
import com.talented.buttie.simulation.domain.SimulationRecurrenceType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import lombok.Builder;

@ApiModel("시뮬레이션 항목 대입 미리보기")
@Builder
public record PreviewItemRequestDTO(
    @ApiModelProperty(value = "시뮬레이션 항목 카테고리", example = "EXPENSE", required = true)
    @NotNull(message = "시뮬레이션 항목 카테고리는 필수입니다.")
    SimulationItemCategory simulationItemCategory,

    @ApiModelProperty(value = "항목 이름", example = "식비 절약", required = true)
    @NotBlank(message = "항목 이름은 필수입니다.")
    String itemName,

    @ApiModelProperty(value = "항목 적용 금액. 정책 항목은 서버에서 policyId로 조회한 금액을 사용합니다.", example = "50000")
    @PositiveOrZero(message = "항목 적용 금액은 0 이상이어야 합니다.")
    Integer amount,

    @ApiModelProperty(value = "항목 적용 시작일", example = "2026-08-01", required = true)
    @NotNull(message = "항목 적용 시작일은 필수입니다.")
    LocalDate applyStartDate,

    @ApiModelProperty(value = "항목 적용 종료일. 일회성 항목은 생략할 수 있습니다.", example = "2026-10-31")
    LocalDate applyEndDate,

    @ApiModelProperty(value = "반복 유형", example = "MONTHLY", required = true)
    @NotNull(message = "반복 유형은 필수입니다.")
    SimulationRecurrenceType recurrenceType,

    @ApiModelProperty(value = "월 반복 적용일. 지출 절약 항목은 생략합니다.", example = "25")
    Integer recurrenceDay,

    @ApiModelProperty(value = "정책 ID. 정책 항목일 때 필수입니다.", example = "1")
    Long policyId,

    @ApiModelProperty(value = "항목 상세 조건 값", example = "식비")
    String detailValue
    ) {
}

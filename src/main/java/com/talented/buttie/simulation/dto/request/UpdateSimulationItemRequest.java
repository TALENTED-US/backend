package com.talented.buttie.simulation.dto.request;

import com.talented.buttie.simulation.domain.SimulationRecurrenceType;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record UpdateSimulationItemRequest(

    @ApiModelProperty(value = "수입 항목 이름. 수입 항목일 때 필수입니다.", example = "쿠팡 단기 알바")
    String itemName,

    @ApiModelProperty(value = "항목 적용 금액", example = "200000", required = true)
    @NotNull(message = "항목 적용 금액은 필수입니다.")
    @Positive(message = "항목 적용 금액은 0보다 커야 합니다.")
    Integer amount,

    @ApiModelProperty(value = "항목 적용 시작일", example = "2026-09-10", required = true)
    @NotNull(message = "항목 적용 시작일은 필수입니다.")
    LocalDate applyStartDate,

    @ApiModelProperty(value = "항목 적용 종료일. MONTHLY는 필수이며, ONCE는 생략 시 시작일과 동일하게 처리됩니다.", example = "2026-12-10")
    LocalDate applyEndDate,

    @ApiModelProperty(value = "반복 유형", example = "MONTHLY", required = true)
    @NotNull(message = "반복 유형은 필수입니다.")
    SimulationRecurrenceType recurrenceType
) {
}

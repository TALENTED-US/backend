package com.talented.buttie.simulation.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import javax.validation.constraints.NotNull;
import lombok.Builder;

@ApiModel(description = "시뮬레이션 수행 기간 수정 요청")
@Builder
public record UpdateSimulationPeriodRequestDTO(

    @ApiModelProperty(value = "시뮬레이션 수행 시작일", example = "2026-08-01", required = true)
    @NotNull(message = "시뮬레이션 수행 시작일은 필수입니다.")
    LocalDate simulationStartDate,

    @ApiModelProperty(value = "시뮬레이션 수행 종료일", example = "2027-01-31", required = true)
    @NotNull(message = "시뮬레이션 수행 종료일은 필수입니다.")
    LocalDate simulationDueDate
) {
}

package com.talented.buttie.simulation.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import lombok.Builder;

@ApiModel(description = "시뮬레이션 생성 요청")
@Builder
public record CreateSimulationRequestDTO(
    @ApiModelProperty(
        value = "시뮬레이션 수행 시작일",
        example = "2026-08-01",
        required = true
    )
    @NotNull(message = "시뮬레이션 수행 시작일은 필수입니다.")
    LocalDate startDate,

    @ApiModelProperty(
        value = "시뮬레이션 종료일",
        example = "2027-01-31",
        required = true
    )
    @NotNull(message = "시뮬레이션 종료일은 필수입니다.")
    LocalDate endDate
) {
    @JsonIgnore
    @AssertTrue(message = "종료일은 시작일 이후여야 합니다.")
    public boolean isValidDateRange() {
        return startDate == null
            || endDate == null
            || endDate.isAfter(startDate);
    }
}

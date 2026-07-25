package com.talented.buttie.simulation.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import lombok.Builder;

@ApiModel(description = "시뮬레이션 생성 요청")
@Builder
public record CreateSimulationRequestDTO(
    @ApiModelProperty(
        value = "시뮬레이션 수행 시작 일시",
        example = "2026-08-01T00:00:00",
        required = true
    )
    @NotNull(message = "시뮬레이션 수행 시작 일시는 필수입니다.")
    LocalDateTime startDate,

    @ApiModelProperty(
        value = "시뮬레이션 종료 일시",
        example = "2027-01-31T00:00:00",
        required = true
    )
    @NotNull(message = "시뮬레이션 종료 일시는 필수입니다.")
    LocalDateTime endDate
) {
    @JsonIgnore
    @AssertTrue(message = "종료 일시는 시작 일시 이후여야 합니다.")
    public boolean isValidDateRange() {
        return startDate == null
            || endDate == null
            || endDate.isAfter(startDate);
    }
}

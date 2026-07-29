package com.talented.buttie.simulation.dto.response;

import com.talented.buttie.simulation.domain.SimulationVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;

@ApiModel(description = "시뮬레이션 생성 응답")
@Builder
public record SimulationResponseDTO(

    @ApiModelProperty(value = "시뮬레이션 ID", example = "1")
    Long simulationId,

    @ApiModelProperty(value = "사용자 ID", example = "1")
    Long userId,

    @ApiModelProperty(value = "기준 스냅샷 ID", example = "10")
    Long snapshotId,

    @ApiModelProperty(value = "시뮬레이션 수행 시작일", example = "2026-08-01")
    LocalDate startDate,

    @ApiModelProperty(value = "시뮬레이션 종료일", example = "2027-01-31")
    LocalDate endDate,

    @ApiModelProperty(value = "종료 예상 잔액", example = "5000000")
    Integer endingBalance,

    @ApiModelProperty(value = "준비 가능 개월", example = "8.25")
    BigDecimal prepMonths
) {
    public static SimulationResponseDTO from(SimulationVO simulation){
        return SimulationResponseDTO.builder()
            .simulationId(simulation.getSimulationId())
            .userId(simulation.getUserId())
            .snapshotId(simulation.getSnapshotId())
            .startDate(simulation.getSimulationStartDate())
            .endDate(simulation.getSimulationDueDate())
            .endingBalance(simulation.getSimulationEndAmount())
            .prepMonths(simulation.getPrepMonths())
            .build();
    }
}

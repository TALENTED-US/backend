package com.talented.buttie.simulation.dto.response;

import com.talented.buttie.simulation.domain.SimulationVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @ApiModelProperty(value = "시뮬레이션 수행 시작 일시", example = "2026-08-01T00:00:00")
    LocalDateTime startDate,

    @ApiModelProperty(value = "시뮬레이션 종료 일시", example = "2027-01-31T00:00:00")
    LocalDateTime endDate,

    @ApiModelProperty(value = "종료 예상 잔액", example = "5000000")
    Integer endingBalance,

    @ApiModelProperty(value = "목표 달성률", example = "35.50")
    BigDecimal targetRate,

    @ApiModelProperty(value = "준비 가능 개월", example = "8.25")
    BigDecimal prepMonths
) {
    public static SimulationResponseDTO from(SimulationVO simulation){
        return SimulationResponseDTO.builder()
            .simulationId(simulation.getSimulationId())
            .userId(simulation.getUserId())
            .snapshotId(simulation.getSnapshotId())
            .startDate(simulation.getStartDate())
            .endDate(simulation.getEndDate())
            .endingBalance(simulation.getEndingBalance())
            .targetRate(simulation.getTargetRate())
            .prepMonths(simulation.getPrepMonths())
            .build();
    }
}

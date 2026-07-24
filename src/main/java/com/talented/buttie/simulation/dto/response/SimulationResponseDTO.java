package com.talented.buttie.simulation.dto.response;

import com.talented.buttie.simulation.domain.SimulationVO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record SimulationResponseDTO(
    Long simulationId,
    Long userId,
    Long snapshotId,
    LocalDateTime startDate,
    LocalDateTime endDate,
    Integer endingBalance,
    BigDecimal targetRate,
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

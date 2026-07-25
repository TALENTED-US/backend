package com.talented.buttie.simulation.domain;

import com.talented.buttie.simulation.dto.request.CreateSimulationRequestDTO;
import com.talented.buttie.simulation.dto.response.SimulationResponseDTO;
import com.talented.buttie.snapshot.dto.result.SimulationSnapshotResultDTO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SimulationVO {
    private Long simulationId;
    private Long userId;
    private Long snapshotId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer endingBalance;
    private BigDecimal targetRate;
    private BigDecimal prepMonths;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private LocalDateTime confirmedAt;
    private Boolean isDeleted;

    public static SimulationVO createCurrentSimulation(
        Long userId,
        CreateSimulationRequestDTO request,
        SimulationSnapshotResultDTO snapshot
    ) {
        return SimulationVO.builder()
            .userId(userId)
            .snapshotId(snapshot.snapshotId())
            .startDate(request.startDate())
            .endDate(request.endDate())
            .endingBalance(snapshot.liquidAssets())  // TODO: 예상 재정 계획 구현(ProjectionEngine) 후 결과로 교체
            .targetRate(snapshot.targetAchievementRate())
            .prepMonths(snapshot.prepPossibleMonths())
            .build();
    }
}

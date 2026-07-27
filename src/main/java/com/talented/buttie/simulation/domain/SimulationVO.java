package com.talented.buttie.simulation.domain;

import com.talented.buttie.simulation.dto.request.CreateSimulationRequestDTO;
import com.talented.buttie.snapshot.domain.FinancialSnapshotVO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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

    private List<MonthlyProjectionVO> monthlyProjections;

    public static SimulationVO createCurrentSimulation(
        Long userId,
        CreateSimulationRequestDTO request,
        FinancialSnapshotVO snapshot
    ) {
        return SimulationVO.builder()
            .userId(userId)
            .snapshotId(snapshot.getSnapshotId())
            .startDate(request.startDate())
            .endDate(request.endDate())
            .endingBalance(snapshot.getLiquidAssets())  // TODO: 예상 재정 계획 구현(ProjectionEngine) 후 결과로 교체
            .targetRate(snapshot.getTargetAchievementRate())
            .prepMonths(snapshot.getPrepPossibleMonths())
            .build();
    }
}

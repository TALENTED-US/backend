package com.talented.buttie.simulation.domain;

import com.talented.buttie.simulation.dto.request.CreateSimulationRequestDTO;
import com.talented.buttie.snapshot.domain.FinancialSnapshotVO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SimulationVO {
    private Long simulationId;
    private Long userId;
    private Long snapshotId;
    private LocalDate simulationStartDate;
    private LocalDate simulationDueDate;
    private Integer simulationEndAmount;
    private BigDecimal prepMonths;
    private LocalDateTime confirmedAt;

    private List<MonthlyProjectionVO> monthlyProjections;

    public static SimulationVO createCurrentSimulation(
        Long userId,
        CreateSimulationRequestDTO request,
        FinancialSnapshotVO snapshot
    ) {
        return SimulationVO.builder()
            .userId(userId)
            .snapshotId(snapshot.getSnapshotId())
            .simulationStartDate(request.simulationStartDate())
            .simulationDueDate(request.simulationDueDate())
            .simulationEndAmount(snapshot.getLiquidAssets())
            .prepMonths(snapshot.getPrepPossibleMonths())
            .build();
    }
}

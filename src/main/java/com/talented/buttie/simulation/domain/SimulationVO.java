package com.talented.buttie.simulation.domain;

import com.talented.buttie.simulation.dto.request.CreateSimulationRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationVO {

    private Long simulationId;
    private Long userId;
    private Long snapshotId;
    private LocalDate simulationStartDate;
    private LocalDate simulationDueDate;
    private Integer simulationEndAmount;
    private BigDecimal expectPrepMonths;
    private LocalDateTime confirmedAt;

    private List<MonthlyProjectionVO> monthlyProjections;

    public static SimulationVO createCurrentSimulation(
        Long userId,
        CreateSimulationRequest request,
        FinancialSnapshotVO snapshot
    ) {
        return SimulationVO.builder()
            .userId(userId)
            .snapshotId(snapshot.getSnapshotId())
            .simulationStartDate(request.simulationStartDate())
            .simulationDueDate(request.simulationDueDate())
            .simulationEndAmount(snapshot.getLiquidAssets())
            .expectPrepMonths(snapshot.getCurrentPrepMonths())
            .build();
    }
}

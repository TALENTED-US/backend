package com.talented.buttie.simulation.domain;

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
        Long snapshotId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer endingBalance,
        BigDecimal targetRate,
        BigDecimal prepMonths
    ) {
        return SimulationVO.builder()
            .userId(userId)
            .snapshotId(snapshotId)
            .startDate(startDate)
            .endDate(endDate)
            .endingBalance(endingBalance)
            .targetRate(targetRate)
            .prepMonths(prepMonths)
            .build();
    }
}

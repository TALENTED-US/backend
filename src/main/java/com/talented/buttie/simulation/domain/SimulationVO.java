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
}

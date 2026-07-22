package com.talented.buttie.simulation.domain;

import java.time.LocalDateTime;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SimulationItemVO {
    private Long simulationItemId;
    private Long simulationId;
    private SimulationItemCategory category;
    private Integer amount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long policyId;
    private Long financeId;
    private String detailValue;
    private SimulationRecurrenceType recurrenceType;
    private Boolean isDeleted;
}

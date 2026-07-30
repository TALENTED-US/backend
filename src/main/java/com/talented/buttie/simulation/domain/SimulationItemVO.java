package com.talented.buttie.simulation.domain;

import java.time.LocalDate;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SimulationItemVO {
    private Long simulationItemId;
    private Long simulationId;
    private SimulationItemCategory simulationItemCategory;
    private Integer simulationItemApplyAmount;
    private LocalDate applyStartDate;
    private LocalDate applyEndDate;
    private Long policyId;
    private String detailValue;
    private SimulationRecurrenceType recurrenceType;
    private Boolean isDeleted;
}

package com.talented.buttie.simulation.domain;

import com.talented.buttie.catalog.domain.PolicyVO;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import java.time.LocalDate;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SimulationItemVO {
    private Long simulationItemId;
    private Long simulationId;
    private SimulationItemCategory simulationItemCategory;
    private String simulationItemName;
    private ExpenseCategory simulationItemExpenseCategory;
    private Integer simulationItemApplyAmount;
    private LocalDate applyStartDate;
    private LocalDate applyEndDate;
    private Long policyId;
    private SimulationRecurrenceType recurrenceType;
    private Boolean isDeleted;
    private PolicyVO policy;
}

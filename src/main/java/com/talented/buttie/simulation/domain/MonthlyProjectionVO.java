package com.talented.buttie.simulation.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MonthlyProjectionVO {
    private Long projectionId;
    private Long simulationId;
    private LocalDate projectionMonth;
    private Integer openingBalance;
    private Integer expectedIncome;
    private Integer expectedExpense;
    private Integer closingBalance;
    private Boolean adjustmentRequired;
    private String adjustmentReason;
}

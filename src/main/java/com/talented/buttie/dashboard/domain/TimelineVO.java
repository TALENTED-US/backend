package com.talented.buttie.dashboard.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineVO {
    private Long simulationId;
    private Long userId;
    private BigDecimal currentPrepMonths;
    private BigDecimal expectPrepMonths;
    private LocalDate targetEmploymentDate;
    private Integer livingFundThreshold;
}

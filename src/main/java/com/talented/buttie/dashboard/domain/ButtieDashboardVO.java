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
public class ButtieDashboardVO {
    private Integer buttieLevel;
    private Integer buttieTotalExp;
    private Integer requiredExp;
    private String buttieImageUrl;
    private String riskLevel;
    private BigDecimal currentPrepMonths;
    private BigDecimal expectPrepMonths;
    private LocalDate targetEmploymentDate;
}

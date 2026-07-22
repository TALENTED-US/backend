package com.talented.buttie.catalog.domain;

import java.math.BigDecimal;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FinanceProductVO {
    private Long financeId;
    private String company;
    private String name;
    private FinanceProductType type;
    private Integer minAge;
    private Integer maxAge;
    private Integer period;
    private BigDecimal baseRate;
    private BigDecimal preferredRate;
    private Integer minimumAmount;
    private String registerCondition;
    private String withdrawCondition;
    private String url;
    private FinanceProductStatus status;
}

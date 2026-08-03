package com.talented.buttie.catalog.domain;

import com.talented.buttie.simulation.domain.SimulationRecurrenceType;
import java.time.LocalDateTime;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PolicyVO {
    private Long policyId;
    private String policyName;
    private String policyCategory;
    private Integer policyMinAge;
    private Integer policyMaxAge;
    private String policyRegion;
    private Integer policySupportAmount;
    private SimulationRecurrenceType policyRecurrenceType;
    private LocalDateTime dueDate;
    private String requiredDocument;
    private String employmentPrepStatus;
    private Integer familyCount;
    private PolicyStatus policyStatus;
    private String policyUrl;
}

package com.talented.buttie.catalog.domain;

import java.time.LocalDate;

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
    private Integer supportMonthCount;
    private LocalDate dueDate;
    private String requiredDocument;
    private String employmentPrepStatus;
    private Integer familyCount;
    private PolicyStatus policyStatus;
    private String policyUrl;
    private String externalSource;
    private String externalPolicyId;
    private AmountParseConfidence amountParseConfidence;
}

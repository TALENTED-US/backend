package com.talented.buttie.catalog.dto.response;

import com.talented.buttie.catalog.domain.AmountParseConfidence;
import com.talented.buttie.catalog.domain.PolicyStatus;
import com.talented.buttie.catalog.domain.PolicyCategory;
import com.talented.buttie.catalog.domain.PolicyVO;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record AdminPolicyResponse(
    Long policyId,
    String policyName,
    PolicyCategory policyCategory,
    Integer policyMinAge,
    Integer policyMaxAge,
    String policyRegion,
    Integer policySupportAmount,
    Integer supportMonthCount,
    LocalDate dueDate,
    String requiredDocument,
    String employmentPrepStatus,
    Integer familyCount,
    PolicyStatus policyStatus,
    String policyUrl,
    String externalSource,
    AmountParseConfidence amountParseConfidence
) {
    public static AdminPolicyResponse from(PolicyVO policy) {
        return AdminPolicyResponse.builder()
            .policyId(policy.getPolicyId())
            .policyName(policy.getPolicyName())
            .policyCategory(policy.getPolicyCategory())
            .policyMinAge(policy.getPolicyMinAge())
            .policyMaxAge(policy.getPolicyMaxAge())
            .policyRegion(policy.getPolicyRegion())
            .policySupportAmount(policy.getPolicySupportAmount())
            .supportMonthCount(policy.getSupportMonthCount())
            .dueDate(policy.getDueDate())
            .requiredDocument(policy.getRequiredDocument())
            .employmentPrepStatus(policy.getEmploymentPrepStatus())
            .familyCount(policy.getFamilyCount())
            .policyStatus(policy.getPolicyStatus())
            .policyUrl(policy.getPolicyUrl())
            .externalSource(policy.getExternalSource())
            .amountParseConfidence(policy.getAmountParseConfidence())
            .build();
    }
}

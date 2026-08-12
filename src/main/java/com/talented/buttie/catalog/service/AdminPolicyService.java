package com.talented.buttie.catalog.service;

import com.talented.buttie.catalog.domain.AmountParseConfidence;
import com.talented.buttie.catalog.domain.PolicyVO;
import com.talented.buttie.catalog.dto.request.AdminPolicyUpsertRequest;
import com.talented.buttie.catalog.dto.response.AdminPolicyListResponse;
import com.talented.buttie.catalog.dto.response.AdminPolicyResponse;
import com.talented.buttie.catalog.exception.CatalogErrorCode;
import com.talented.buttie.catalog.mapper.PolicyMapper;
import com.talented.buttie.catalog.mapper.PolicyRegionMapMapper;
import com.talented.buttie.common.exception.ApplicationException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminPolicyService {

    private final PolicyMapper policyMapper;
    private final PolicyRegionMapMapper policyRegionMapMapper;

    public AdminPolicyListResponse getPolicies(String externalSource, AmountParseConfidence amountParseConfidence, int page, int size) {
        int offset = (page - 1) * size;
        String confidenceCondition = amountParseConfidence == null ? null : amountParseConfidence.name();

        List<AdminPolicyResponse> policies = policyMapper.search(externalSource, confidenceCondition, offset, size).stream()
            .map(AdminPolicyResponse::from)
            .toList();
        int totalCount = policyMapper.countBySearch(externalSource, confidenceCondition);

        return AdminPolicyListResponse.builder()
            .policies(policies)
            .totalCount(totalCount)
            .build();
    }

    @Transactional
    public AdminPolicyResponse createPolicy(AdminPolicyUpsertRequest request) {
        PolicyVO policy = toPolicyVO(request);
        policyMapper.insert(policy);
        return AdminPolicyResponse.from(policy);
    }

    @Transactional
    public AdminPolicyResponse updatePolicy(Long policyId, AdminPolicyUpsertRequest request) {
        PolicyVO policy = toPolicyVO(request);
        policy.setPolicyId(policyId);
        int updatedRows = policyMapper.update(policy);
        if (updatedRows == 0) {
            throw ApplicationException.from(CatalogErrorCode.POLICY_NOT_FOUND);
        }
        return AdminPolicyResponse.from(policy);
    }

    @Transactional
    public void deletePolicy(Long policyId) {
        policyRegionMapMapper.deleteByPolicyId(policyId);
        int deletedRows = policyMapper.deleteById(policyId);
        if (deletedRows == 0) {
            throw ApplicationException.from(CatalogErrorCode.POLICY_NOT_FOUND);
        }
    }

    private PolicyVO toPolicyVO(AdminPolicyUpsertRequest request) {
        return PolicyVO.builder()
            .externalSource(null)
            .policyName(request.policyName())
            .policyCategory(request.policyCategory())
            .policyMinAge(request.policyMinAge())
            .policyMaxAge(request.policyMaxAge())
            .policyRegion(request.policyRegion())
            .policySupportAmount(request.policySupportAmount())
            .supportMonthCount(request.supportMonthCount() == null ? 1 : request.supportMonthCount())
            .dueDate(request.dueDate())
            .requiredDocument(request.requiredDocument())
            .employmentPrepStatus(request.employmentPrepStatus())
            .familyCount(request.familyCount() == null ? 1 : request.familyCount())
            .policyStatus(request.policyStatus())
            .policyUrl(request.policyUrl())
            .amountParseConfidence(AmountParseConfidence.MANUAL)
            .build();
    }
}

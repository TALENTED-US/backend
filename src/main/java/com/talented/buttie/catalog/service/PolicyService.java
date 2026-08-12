package com.talented.buttie.catalog.service;

import com.talented.buttie.catalog.domain.PolicyVO;
import com.talented.buttie.catalog.exception.CatalogErrorCode;
import com.talented.buttie.catalog.mapper.PolicyMapper;
import com.talented.buttie.common.exception.ApplicationException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyMapper policyMapper;

    @Transactional(readOnly = true)
    public List<PolicyVO> getAllPolicies() {
        List<PolicyVO> policies;

        try {
            policies = policyMapper.findAll();
        } catch (Exception e) {
            throw ApplicationException.from(CatalogErrorCode.POLICY_FETCH_FAILED);
        }

        if (policies == null || policies.isEmpty()) {
            throw ApplicationException.from(CatalogErrorCode.POLICY_LIST_EMPTY);
        }

        for (PolicyVO policy : policies) {
            if (policy == null || policy.getPolicyName() == null || policy.getPolicyName().isBlank()) {
                throw ApplicationException.from(CatalogErrorCode.INVALID_POLICY_DATA);
            }
        }

        return policies;
    }
}

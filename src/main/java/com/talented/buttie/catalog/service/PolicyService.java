package com.talented.buttie.catalog.service;

import com.talented.buttie.catalog.domain.PolicyVO;
import com.talented.buttie.catalog.dto.request.PolicySearchRequest;
import com.talented.buttie.catalog.dto.response.PolicyResponse;
import com.talented.buttie.catalog.exception.CatalogErrorCode;
import com.talented.buttie.catalog.mapper.PolicyMapper;
import com.talented.buttie.common.dto.PageResponse;
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
    public PageResponse<PolicyResponse> getAllPolicies(int page, int size) {
        int validPage = Math.max(page, 1);
        int validSize = Math.max(size, 1);
        int offset = (validPage - 1) * validSize;

        List<PolicyVO> policies;
        long totalElements;

        try {
            policies = policyMapper.findAllPaginated(offset, validSize);
            totalElements = policyMapper.countAll();
        } catch (Exception e) {
            throw ApplicationException.from(CatalogErrorCode.POLICY_FETCH_FAILED);
        }

        List<PolicyResponse> responseList = (policies != null)
            ? policies.stream().map(PolicyResponse::from).toList()
            : List.of();

        return PageResponse.of(responseList, validPage, validSize, totalElements);
    }

    @Transactional(readOnly = true)
    public PageResponse<PolicyResponse> searchPolicies(Long userId, PolicySearchRequest request) {
        PolicySearchRequest raw = request != null ? request : PolicySearchRequest.builder().build();

        PolicySearchRequest searchRequest = PolicySearchRequest.builder()
            .keyword(raw.keyword() != null ? raw.keyword().trim() : null)
            .policyCategory(raw.policyCategory() != null ? raw.policyCategory().trim() : null)
            .policyRegion(raw.policyRegion() != null ? raw.policyRegion().trim() : null)
            .age(raw.age())
            .policySupportAmount(raw.policySupportAmount())
            .dueDate(raw.dueDate())
            .dueDateFilter(raw.dueDateFilter() != null ? raw.dueDateFilter().trim() : null)
            .employmentPrepStatus(raw.employmentPrepStatus() != null ? raw.employmentPrepStatus().trim() : null)
            .policyStatus(raw.policyStatus())
            .page(raw.page())
            .size(raw.size())
            .build();

        int validPage = searchRequest.getPageNumber();
        int validSize = searchRequest.getPageSize();
        int offset = searchRequest.getOffset();

        List<PolicyVO> policies;
        long totalElements;

        try {
            policies = policyMapper.searchPolicies(searchRequest, offset, validSize);
            totalElements = policyMapper.countSearchPolicies(searchRequest);
        } catch (Exception e) {
            throw ApplicationException.from(CatalogErrorCode.POLICY_FETCH_FAILED);
        }

        List<PolicyResponse> responseList = (policies != null)
            ? policies.stream().map(PolicyResponse::from).toList()
            : List.of();

        return PageResponse.of(responseList, validPage, validSize, totalElements);
    }
}

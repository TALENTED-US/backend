package com.talented.buttie.simulation.service.llm;

import com.github.benmanes.caffeine.cache.Cache;
import com.talented.buttie.catalog.dto.request.PolicySearchRequest;
import com.talented.buttie.catalog.dto.response.PolicyResponse;
import com.talented.buttie.catalog.mapper.PolicyMapper;
import com.talented.buttie.user.domain.EmploymentPreparationVO;
import com.talented.buttie.user.mapper.EmploymentPreparationMapper;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PolicySuggestionService {

    private static final int RECOMMENDATION_SIZE = 5;

    private final PolicyMapper policyMapper;
    private final EmploymentPreparationMapper employmentPreparationMapper;
    private final Cache<String, List<PolicyResponse>> policyRecommendationCache;

    public List<PolicyResponse> find(Long userId, String prompt) {
        EmploymentPreparationVO preparation = employmentPreparationMapper.selectEmploymentPreparation(userId);
        String region = preparation == null ? null : preparation.getEmploymentPrepRegion();
        Integer age = resolveAge(preparation);
        String status = preparation == null || preparation.getEmploymentPrepType() == null
            ? null : preparation.getEmploymentPrepType().name();
        String cacheKey = "policy-recommendation:" + userId + ":" + nullSafe(region) + ":"
            + nullSafe(status) + ":" + nullSafe(age) + ":" + Integer.toHexString(nullSafe(prompt).hashCode());

        List<PolicyResponse> cached = policyRecommendationCache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }

        String category = resolveCategory(prompt);
        PolicySearchRequest request = PolicySearchRequest.builder()
            .keyword(category == null ? normalize(prompt) : null)
            .policyCategory(category)
            .policyRegion(region)
            .age(age)
            .employmentPrepStatus(status)
            .page(1)
            .size(RECOMMENDATION_SIZE)
            .build();

        List<PolicyResponse> results = policyMapper.searchPolicies(request, 0, RECOMMENDATION_SIZE).stream()
            .map(PolicyResponse::from)
            .toList();
        policyRecommendationCache.put(cacheKey, results);
        return results;
    }

    private Integer resolveAge(EmploymentPreparationVO preparation) {
        if (preparation == null || preparation.getBirthDate() == null) {
            return null;
        }
        return Period.between(preparation.getBirthDate(), LocalDate.now()).getYears();
    }

    private String resolveCategory(String prompt) {
        String value = normalize(prompt);
        if (value.contains("월세") || value.contains("주거") || value.contains("전세")) {
            return "HOUSING";
        }
        if (value.contains("자격증") || value.contains("교육") || value.contains("학원")) {
            return "EDUCATION";
        }
        if (value.contains("취업") || value.contains("면접") || value.contains("구직")) {
            return "EMPLOYMENT";
        }
        if (value.contains("교통")) {
            return "TRANSPORT";
        }
        return null;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String nullSafe(Object value) {
        return value == null ? "" : value.toString();
    }
}

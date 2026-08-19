package com.talented.buttie.simulation.service.llm;

import com.github.benmanes.caffeine.cache.Cache;
import com.talented.buttie.catalog.dto.request.PolicySearchRequest;
import com.talented.buttie.common.util.CacheKeyUtils;
import com.talented.buttie.catalog.dto.response.PolicyResponse;
import com.talented.buttie.catalog.service.PolicyService;
import com.talented.buttie.user.domain.EmploymentPreparationVO;
import com.talented.buttie.user.mapper.EmploymentPreparationMapper;
import com.talented.buttie.simulation.domain.FinancialSnapshotVO;
import com.talented.buttie.simulation.dto.response.recommendation.PolicyRecommendationReasonResult;
import com.talented.buttie.simulation.mapper.FinancialSnapshotMapper;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PolicySuggestionService {

    private static final int RECOMMENDATION_SIZE = 5;

    private final PolicyService policyService;
    private final EmploymentPreparationMapper employmentPreparationMapper;
    private final FinancialSnapshotMapper financialSnapshotMapper;
    private final PolicyRecommendationPromptFactory policyRecommendationPromptFactory;
    private final OpenAiChatClient openAiChatClient;
    private final Cache<String, List<PolicyResponse>> policyRecommendationCache;

    public List<PolicyResponse> find(Long userId, String prompt) {
        EmploymentPreparationVO preparation = employmentPreparationMapper.selectEmploymentPreparation(userId);
        String region = preparation == null ? null : preparation.getEmploymentPrepRegion();
        Integer age = resolveAge(preparation);
        String status = preparation == null || preparation.getEmploymentPrepType() == null
            ? null : preparation.getEmploymentPrepType().name();
        String cacheKey = "policy-recommendation:v3:" + userId + ":" + nullSafe(region) + ":"
            + nullSafe(status) + ":" + nullSafe(age) + ":" + CacheKeyUtils.sha256(nullSafe(prompt));

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

        log.debug("AI 정책 추천 조회 조건: userId={}, region={}, age={}, employmentPrepStatus={}, category={}",
            userId, region, age, status, category);
        List<PolicyResponse> results = search(request);
        log.debug("AI 정책 추천 조회 결과: userId={}, count={}", userId, results.size());
        List<PolicyResponse> enrichedResults = enrichWithRecommendationReasons(
            preparation,
            financialSnapshotMapper.findLatestByUserId(userId),
            results
        );
        policyRecommendationCache.put(cacheKey, enrichedResults);
        return enrichedResults;
    }

    public void invalidateForUser(Long userId) {
        String keyPrefix = "policy-recommendation:v3:" + userId + ":";
        policyRecommendationCache.asMap().keySet().removeIf(key -> key.startsWith(keyPrefix));
    }

    private List<PolicyResponse> search(PolicySearchRequest request) {
        return policyService.searchPolicies(null, request).content();
    }

    private List<PolicyResponse> enrichWithRecommendationReasons(
        EmploymentPreparationVO preparation,
        FinancialSnapshotVO snapshot,
        List<PolicyResponse> policies
    ) {
        if (policies.isEmpty()) return policies;

        PolicyRecommendationReasonResult result = openAiChatClient.createPolicyRecommendationReasons(
            policyRecommendationPromptFactory.create(preparation, snapshot, policies)
        );
        return java.util.stream.IntStream.range(0, policies.size())
            .mapToObj(index -> {
                PolicyResponse policy = policies.get(index);
                String reason = findReason(result, index);
                return withRecommendationReason(policy,
                    reason == null ? createDefaultRecommendationReason(preparation, policy) : reason);
            })
            .toList();
    }

    private String findReason(PolicyRecommendationReasonResult result, int index) {
        if (result == null || result.getReasons() == null) return null;
        return result.getReasons().stream()
            .filter(reason -> reason.getIndex() != null && reason.getIndex() == index)
            .map(PolicyRecommendationReasonResult.Reason::getRecommendationReason)
            .filter(reason -> reason != null && !reason.isBlank())
            .findFirst()
            .orElse(null);
    }

    private String createDefaultRecommendationReason(
        EmploymentPreparationVO preparation,
        PolicyResponse policy
    ) {
        String policyName = policy.policyName() == null ? "이 정책" : policy.policyName();
        String userCondition = describeUserCondition(preparation);
        String support = describeSupport(policy);
        if (requiresAdditionalConfirmation(policy)) {
            return "[추가 조건 확인] " + userCondition + "을 고려했을 때, " + policyName
                + "의 " + support + "은 도움이 될 수 있어요. 다만 면접 참여 및 증빙서류 보유 여부를 "
                + "확인한 뒤 신청을 검토하세요.";
        }
        return "[가장 적합] " + userCondition + "을 고려했을 때, " + policyName + "의 " + support
            + " 조건에 부합할 수 있으므로 재정 부담을 줄이기 위해 신청을 검토하는 것을 추천합니다.";
    }

    private String describeUserCondition(EmploymentPreparationVO preparation) {
        if (preparation == null) return "현재 입력한 사용자 조건";
        String region = preparation.getEmploymentPrepRegion();
        String employmentType = preparation.getEmploymentPrepType() == null ? null
            : switch (preparation.getEmploymentPrepType()) {
                case FIRST_JOB -> "첫 직장을 준비하는";
                case REEMPLOYMENT -> "재취업을 준비하는";
            };
        if (region != null && !region.isBlank() && employmentType != null) {
            return region + "에 거주하며 " + employmentType + " 사용자";
        }
        if (region != null && !region.isBlank()) return region + "에 거주하는 사용자";
        if (employmentType != null) return employmentType + " 사용자";
        return "현재 입력한 사용자 조건";
    }

    private String describeSupport(PolicyResponse policy) {
        Integer amount = policy.policySupportAmount();
        Integer monthCount = policy.supportMonthCount();
        if (amount != null && monthCount != null && monthCount > 1) {
            return "월 " + String.format("%,d", amount) + "원을 " + monthCount + "개월 지원하는";
        }
        if (amount != null) return "최대 " + String.format("%,d", amount) + "원을 지원하는";
        return "지원 내용을 제공하는";
    }

    private boolean requiresAdditionalConfirmation(PolicyResponse policy) {
        String source = (policy.policyName() == null ? "" : policy.policyName()) + " "
            + (policy.requiredDocument() == null ? "" : policy.requiredDocument());
        return source.contains("면접");
    }

    private PolicyResponse withRecommendationReason(PolicyResponse policy, String recommendationReason) {
        return PolicyResponse.builder()
            .policyId(policy.policyId())
            .policyName(policy.policyName())
            .policySupportAmount(policy.policySupportAmount())
            .supportMonthCount(policy.supportMonthCount())
            .dueDate(policy.dueDate())
            .requiredDocument(policy.requiredDocument())
            .policyUrl(policy.policyUrl())
            .policyStatus(policy.policyStatus())
            .recommendationReason(recommendationReason)
            .build();
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

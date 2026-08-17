package com.talented.buttie.simulation.service.llm;

import com.talented.buttie.catalog.dto.response.PolicyResponse;
import com.talented.buttie.simulation.dto.response.recommendation.AiRecommendationBundleResponse;
import com.talented.buttie.simulation.dto.response.recommendation.FinancialRecommendationResponse;
import com.talented.buttie.simulation.dto.response.recommendation.IncomeJobSearchResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiRecommendationFacade {

    private final FinancialRecommendationService financialRecommendationService;
    private final IncomeJobSearchLinkService incomeJobSearchLinkService;
    private final PolicySuggestionService policySuggestionService;

    public AiRecommendationBundleResponse getDefault(Long userId) {
        return createBundle(userId, null);
    }

    public AiRecommendationBundleResponse getCustom(Long userId, String prompt) {
        return createBundle(userId, prompt);
    }

    public FinancialRecommendationResponse getExpense(Long userId, String prompt) {
        return financialRecommendationService.getRecommendations(userId, prompt, RecommendationFocus.EXPENSE);
    }

    public IncomeJobSearchResponse getIncome(Long userId, String prompt) {
        return incomeJobSearchLinkService.create(userId, prompt);
    }

    public List<PolicyResponse> getPolicies(Long userId, String prompt) {
        return policySuggestionService.find(userId, prompt);
    }

    private AiRecommendationBundleResponse createBundle(Long userId, String prompt) {
        return AiRecommendationBundleResponse.builder()
            .financialRecommendation(financialRecommendationService.getRecommendations(userId, prompt, RecommendationFocus.ALL))
            .incomeRecommendation(incomeJobSearchLinkService.create(userId, prompt))
            .policyRecommendations(policySuggestionService.find(userId, prompt))
            .build();
    }
}

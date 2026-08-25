package com.talented.buttie.simulation.dto.response.recommendation;

import com.talented.buttie.catalog.dto.response.PolicyResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRecommendationBundleResponse {

    private FinancialRecommendationResponse financialRecommendation;
    private IncomeJobSearchResponse incomeRecommendation;
    private List<PolicyResponse> policyRecommendations;
}

package com.talented.buttie.simulation.dto.response.recommendation;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialRecommendationResponse {

    private String summary;
    private List<RecommendationItem> recommendations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationItem {
        private RecommendationActionType actionType;
        private String title;
        private String category;
        private Integer suggestedMonthlyAmount;
        private String reason;
    }
}

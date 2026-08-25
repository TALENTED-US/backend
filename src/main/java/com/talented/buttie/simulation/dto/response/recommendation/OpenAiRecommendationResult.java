package com.talented.buttie.simulation.dto.response.recommendation;

import java.util.List;
import lombok.Data;

@Data
public class OpenAiRecommendationResult {

    private String summary;
    private List<Recommendation> recommendations;

    @Data
    public static class Recommendation {
        private RecommendationActionType actionType;
        private String title;
        private String category;
        private Integer suggestedMonthlyAmount;
        private String reason;
    }
}

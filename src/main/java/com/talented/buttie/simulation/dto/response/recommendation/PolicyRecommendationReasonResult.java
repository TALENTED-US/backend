package com.talented.buttie.simulation.dto.response.recommendation;

import java.util.List;
import lombok.Data;

@Data
public class PolicyRecommendationReasonResult {

    private List<Reason> reasons;

    @Data
    public static class Reason {
        private Integer index;
        private String recommendationReason;
    }
}

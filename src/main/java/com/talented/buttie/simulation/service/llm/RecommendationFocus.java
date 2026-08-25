package com.talented.buttie.simulation.service.llm;

public enum RecommendationFocus {
    ALL("REDUCE_EXPENSE 항목만 제안한다."),
    EXPENSE("REDUCE_EXPENSE 항목만 제안한다.");

    private final String description;

    RecommendationFocus(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}

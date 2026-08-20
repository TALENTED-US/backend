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
public class IncomeJobSearchResponse {

    private String searchKeyword;
    private String region;
    private String notice;
    private Integer requiredMonthlyIncome;
    private Integer appliedMonthlyExpenseReduction;
    private Integer availableHoursPerWeek;
    private Integer recommendedMinimumHourlyWage;
    private List<JobSearchLink> links;
    private List<JobPosting> jobs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobSearchLink {
        private String platform;
        private String url;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobPosting {
        private String title;
        private String company;
        private String region;
        private String pay;
        private String employmentType;
        private String url;
    }
}

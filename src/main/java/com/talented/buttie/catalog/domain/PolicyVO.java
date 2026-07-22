package com.talented.buttie.catalog.domain;

import java.time.LocalDateTime;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PolicyVO {
    private Long policyId;
    private String name;
    private String category;
    private Integer minAge;
    private Integer maxAge;
    private String region;
    private Integer amount;
    private LocalDateTime dueDate;
    private String document;
    private String employmentStatus;
    private Integer familyCount;
    private PolicyStatus status;
    private String url;
}

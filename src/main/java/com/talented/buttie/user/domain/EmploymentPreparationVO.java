package com.talented.buttie.user.domain;

import java.time.LocalDate;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmploymentPreparationVO {

    private Long userId;
    @ToString.Exclude
    private LocalDate birthDate;
    private String region;
    private Integer familyCount;
    private EmploymentPreparationType employmentPrepType;
    private LocalDate prepStartDate;
    private LocalDate targetEmploymentDate;
    private Integer livingFundThreshold;
    private Integer emergencyFundThreshold;
}

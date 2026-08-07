package com.talented.buttie.user.domain;

import java.time.LocalDate;
import lombok.*;
import com.talented.buttie.user.dto.request.user.UpdateEmploymentPreparationRequest;
import com.talented.buttie.user.dto.request.user.CreateEmploymentPreparationRequest;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmploymentPreparationVO {

    private Long userId;
    @ToString.Exclude
    private LocalDate birthDate;
    private String employmentPrepRegion;
    private Integer familyCount;
    private EmploymentPreparationType employmentPrepType;
    private LocalDate prepStartDate;
    private LocalDate targetEmploymentDate;
    private Integer livingFundThreshold;

    public static EmploymentPreparationVO createEmploymentPreparation(
        Long userId,
        CreateEmploymentPreparationRequest request
    ) {
        return EmploymentPreparationVO.builder()
            .userId(userId)
            .birthDate(request.birthDate())
            .employmentPrepRegion(request.region())
            .familyCount(request.familyCount())
            .employmentPrepType(request.employmentPrepType())
            .prepStartDate(request.prepStartDate())
            .targetEmploymentDate(request.targetEmploymentDate())
            .livingFundThreshold(request.livingFundThreshold())
            .build();
    }
    public static EmploymentPreparationVO createEmploymentPreparation(
        Long userId,
        UpdateEmploymentPreparationRequest request
    ) {
        return EmploymentPreparationVO.builder()
            .userId(userId)
            .birthDate(request.birthDate())
            .employmentPrepRegion(request.region())
            .familyCount(request.familyCount())
            .employmentPrepType(request.employmentPrepType())
            .prepStartDate(request.prepStartDate())
            .targetEmploymentDate(request.targetEmploymentDate())
            .livingFundThreshold(request.livingFundThreshold())
            .build();
    }
}

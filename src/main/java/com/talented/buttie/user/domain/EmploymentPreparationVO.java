package com.talented.buttie.user.domain;

import java.time.LocalDate;
import lombok.*;
import com.talented.buttie.user.dto.request.UpdateEmploymentPreparationRequestDTO;
import com.talented.buttie.user.dto.request.CreateEmploymentPreparationRequestDTO;
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
        CreateEmploymentPreparationRequestDTO request
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
        UpdateEmploymentPreparationRequestDTO request
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

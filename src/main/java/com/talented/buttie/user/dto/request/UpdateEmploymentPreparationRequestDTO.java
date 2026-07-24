package com.talented.buttie.user.dto.request;

import com.talented.buttie.user.domain.EmploymentPreparationType;
import java.time.LocalDate;

public record UpdateEmploymentPreparationRequestDTO(

    LocalDate birthDate,
    String region,
    Integer familyCount,
    EmploymentPreparationType employmentPrepType,
    LocalDate prepStartDate,
    LocalDate targetEmploymentDate,
    Integer livingFundThreshold
) {}

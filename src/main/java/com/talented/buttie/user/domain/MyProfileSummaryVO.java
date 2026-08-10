package com.talented.buttie.user.domain;

import com.talented.buttie.mydata.domain.ConnectionStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MyProfileSummaryVO {

    private String buttieImageUrl;
    private Integer buttieLevel;
    private Integer buttieTotalExp;
    private Integer requiredExp;
    private String riskLevel;
    private String userNickname;
    private String userEmail;
    private EmploymentPreparationType employmentPrepType;
    private LocalDate prepStartDate;
    private LocalDate targetEmploymentDate;
    private ConnectionStatus mydataStatus;
    private LocalDateTime lastSyncedAt;
}

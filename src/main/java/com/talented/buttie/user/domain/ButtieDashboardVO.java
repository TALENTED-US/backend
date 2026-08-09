package com.talented.buttie.user.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ButtieDashboardVO {

    private int buttieLevel;
    private String stageName;
    private String levelDescription;
    private int buttieTotalExp;
    private int requiredExp;
    private String riskLevel;
    private String buttieImageUrl;
}

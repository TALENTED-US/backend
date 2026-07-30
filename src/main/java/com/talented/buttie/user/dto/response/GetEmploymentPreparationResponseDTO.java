package com.talented.buttie.user.dto.response;

import com.talented.buttie.user.domain.EmploymentPreparationType;
import com.talented.buttie.user.domain.EmploymentPreparationVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;

@ApiModel(description = "취업 준비 정보 조회 응답")
public record GetEmploymentPreparationResponseDTO(

    @ApiModelProperty(value = "취업 준비 유형", example = "FIRST_JOB")
    EmploymentPreparationType employmentPrepType,

    @ApiModelProperty(value = "준비 시작일", example = "2026-07-26")
    LocalDate prepStartDate,

    @ApiModelProperty(value = "목표 취업일", example = "2126-07-26")
    LocalDate targetEmploymentDate,

    @ApiModelProperty(value = "거주 지역", example = "서울특별시")
    String region,

    @ApiModelProperty(value = "세대원 수", example = "1")
    Integer familyCount
) {
    public static GetEmploymentPreparationResponseDTO from(EmploymentPreparationVO vo) {
        return new GetEmploymentPreparationResponseDTO(
            vo.getEmploymentPrepType(),
            vo.getPrepStartDate(),
            vo.getTargetEmploymentDate(),
            vo.getEmploymentPrepRegion(),
            vo.getFamilyCount()
        );
    }
}
//그냥 앞에거랑 합칠게요,,
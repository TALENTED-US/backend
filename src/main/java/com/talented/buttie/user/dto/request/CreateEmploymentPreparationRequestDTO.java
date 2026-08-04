package com.talented.buttie.user.dto.request;

import com.talented.buttie.user.domain.EmploymentPreparationType;
import java.time.LocalDate;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "취업 준비 정보 등록 요청")
public record CreateEmploymentPreparationRequestDTO(

    @ApiModelProperty(value = "생년월일", example = "2002-03-29", required = true)
    @NotNull(message = "생년월일은 필수입니다.")
    LocalDate birthDate,

    @ApiModelProperty(value = "거주 지역", example = "서울특별시", required = true)
    @NotBlank(message = "거주 지역은 필수입니다.")
    String region,

    @ApiModelProperty(value = "세대원 수", example = "1", required = true)
    @NotNull(message = "세대원 수는 필수입니다.")
    Integer familyCount,

    @ApiModelProperty(value = "취업 준비 유형", example = "FIRST_JOB", required = true)
    @NotNull(message = "취업 준비 유형은 필수입니다.")
    EmploymentPreparationType employmentPrepType,

    @ApiModelProperty(value = "취업 준비 시작일", example = "2026-07-01", required = true)
    @NotNull(message = "취업 준비 시작일은 필수입니다.")
    LocalDate prepStartDate,

    @ApiModelProperty(value = "목표 취업일", example = "2026-12-31", required = true)
    @NotNull(message = "목표 취업일은 필수입니다.")
    LocalDate targetEmploymentDate,

    @ApiModelProperty(value = "생활 자금 기준", example = "0", required = true)
    @NotNull(message = "생활 자금 기준은 필수입니다.")
    Integer livingFundThreshold
) {}
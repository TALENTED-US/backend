package com.talented.buttie.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.talented.buttie.dashboard.domain.ButtieDashboardVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;

@ApiModel(description = "홈 화면 버티 대시보드 조회 응답")
@Builder
public record ButtieDashboardResponse(

    @ApiModelProperty(value = "버티 레벨", example = "1")
    Integer buttieLevel,

    @ApiModelProperty(value = "누적 경험치", example = "19")
    Integer buttieTotalExp,

    @ApiModelProperty(value = "다음 레벨 목표 경험치 (최대 레벨은 null)", example = "50")
    Integer requiredExp,

    @ApiModelProperty(value = "버티 이미지 URL", example = "https://cdn.buttie.com/buttie/lv1_stable.png")
    String buttieImageUrl,

    @ApiModelProperty(value = "재정 위험 수준", example = "STABLE")
    String riskLevel,

    @ApiModelProperty(value = "현재 자금으로 버틸 수 있는 개월 수", example = "1.9")
    BigDecimal currentPrepMonths,

    @ApiModelProperty(value = "확정 시나리오 기준 예상 버티는 기간 (확정본 없으면 null)", example = "4.0")
    BigDecimal expectPrepMonths,

    @ApiModelProperty(value = "목표 취업일", example = "2027-01-01")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate targetEmploymentDate
) {
    public static ButtieDashboardResponse from(ButtieDashboardVO vo) {
        return ButtieDashboardResponse.builder()
            .buttieLevel(vo.getButtieLevel())
            .buttieTotalExp(vo.getButtieTotalExp())
            .requiredExp(vo.getRequiredExp())
            .buttieImageUrl(vo.getButtieImageUrl())
            .riskLevel(vo.getRiskLevel())
            .currentPrepMonths(vo.getCurrentPrepMonths())
            .expectPrepMonths(vo.getExpectPrepMonths())
            .targetEmploymentDate(vo.getTargetEmploymentDate())
            .build();
    }
}

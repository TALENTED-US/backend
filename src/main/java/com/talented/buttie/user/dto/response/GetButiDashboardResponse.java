package com.talented.buttie.user.dto.response;
import com.talented.buttie.user.domain.ButiDashboardVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "버티 성장 대시보드 조회 응답")
public record GetButiDashboardResponse (
    @ApiModelProperty(value="현재 레벨",example="1",required = true)
    int buttieLevel,

    @ApiModelProperty(value="단계명",example="새싹 버티",required = true)
    String stageName,

    @ApiModelProperty(value="단계 설명",example="이제 막 자산관리를 시작한 기본 버티",required = true)
    String levelDescription,

    @ApiModelProperty(value="누적 경험치",example="900",required = true)
    int buttieTotalExp,

    @ApiModelProperty(value="필요 누적 경험치",example="100",required = true)
    int requiredExp,

    @ApiModelProperty(value="재정 위험 수준",example="STABLE",required = true)
    String riskLevel,

    @ApiModelProperty(value="버티 이미지 URL",example="https://cdn.buttie.com/buttie/lv1_stable.png",required = true)
    String buttieImageUrl


){
    public static GetButiDashboardResponse from(ButiDashboardVO vo){
        return new GetButiDashboardResponse(
            vo.getButtieLevel(),
            vo.getStageName(),
            vo.getLevelDescription(),
            vo.getButtieTotalExp(),
            vo.getRequiredExp(),
            vo.getRiskLevel(),
            vo.getButtieImageUrl()
        );
    }
}

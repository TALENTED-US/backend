package com.talented.buttie.mydata.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "연결 가능한 금융기관과 보유 자산 수")
public record MydataInstitutionResponse(
    @ApiModelProperty(value = "금융기관명")
    String institutionName,
    @ApiModelProperty(value = "보유 계좌 수")
    int accountCount,
    @ApiModelProperty(value = "보유 체크카드 수")
    int cardCount
) {
}

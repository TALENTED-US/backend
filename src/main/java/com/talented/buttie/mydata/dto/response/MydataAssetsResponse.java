package com.talented.buttie.mydata.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.Builder;

@Builder
@ApiModel(description = "마이데이터 계좌 및 체크카드 목록")
public record MydataAssetsResponse(
    @ApiModelProperty(value = "계좌 목록")
    List<MydataAccountResponse> accounts,
    @ApiModelProperty(value = "체크카드 목록")
    List<MydataCardResponse> cards
) {
}

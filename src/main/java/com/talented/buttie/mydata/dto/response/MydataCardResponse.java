package com.talented.buttie.mydata.dto.response;

import com.talented.buttie.mydata.client.dto.MydataCardData;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;

@Builder
@ApiModel(description = "마이데이터 체크카드 목록 항목")
public record MydataCardResponse(
    @ApiModelProperty(value = "마이데이터 카드 식별값")
    String cardId,
    @ApiModelProperty(value = "카드사명")
    String institutionName,
    @ApiModelProperty(value = "카드 상품명")
    String cardName,
    @ApiModelProperty(value = "마스킹 카드번호")
    String cardNumberMasked,
    @ApiModelProperty(value = "카드 구분 코드", example = "02")
    String cardType,
    @ApiModelProperty(value = "전송요구 여부")
    Boolean isConsent
) {

    public static MydataCardResponse from(MydataCardData data) {
        return MydataCardResponse.builder()
            .cardId(data.getCardId())
            .institutionName(data.getInstitutionName())
            .cardName(data.getCardName())
            .cardNumberMasked(data.getCardNumberMasked())
            .cardType(data.getCardType())
            .isConsent(data.getIsConsent())
            .build();
    }
}

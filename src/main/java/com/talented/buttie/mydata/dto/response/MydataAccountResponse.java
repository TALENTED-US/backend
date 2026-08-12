package com.talented.buttie.mydata.dto.response;

import com.talented.buttie.mydata.client.dto.MydataAccountData;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;

@Builder
@ApiModel(description = "마이데이터 계좌 목록 항목")
public record MydataAccountResponse(
    @ApiModelProperty(value = "마이데이터 계좌 식별값")
    String accountId,
    @ApiModelProperty(value = "금융기관명")
    String institutionName,
    @ApiModelProperty(value = "계좌 상품명")
    String accountName,
    @ApiModelProperty(value = "계좌 구분 코드")
    String accountType,
    @ApiModelProperty(value = "마스킹 계좌번호")
    String accountNumberMasked,
    @ApiModelProperty(value = "계좌 잔액")
    Integer balance,
    @ApiModelProperty(value = "전송요구 여부")
    Boolean isConsent
) {

    public static MydataAccountResponse from(MydataAccountData data) {
        return MydataAccountResponse.builder()
            .accountId(data.getAccountNum())
            .institutionName(data.getInstitutionName())
            .accountName(data.getProductName())
            .accountType(data.getAccountType())
            .accountNumberMasked(data.getAccountNumberMasked())
            .balance(data.getBalanceAmount())
            .isConsent(data.getIsConsent())
            .build();
    }
}

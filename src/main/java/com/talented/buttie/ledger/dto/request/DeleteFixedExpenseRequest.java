package com.talented.buttie.ledger.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import lombok.Builder;

@ApiModel(description = "고정 지출 삭제(해제) 요청")
@Builder
public record DeleteFixedExpenseRequest(
    @ApiModelProperty(value = "암호화된 거래 ID", example = "exp123...", required = true)
    @NotBlank(message = "거래 ID는 필수입니다.")
    String transactionId
) {

}

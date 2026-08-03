package com.talented.buttie.ledger.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Builder;

@ApiModel(description = "수동 거래 내역 삭제 요청")
@Builder
public record DeleteTransactionRequest(
    @ApiModelProperty(value = "삭제할 거래 ID", example = "1", required = true)
    @NotNull(message = "거래 ID는 필수입니다.")
    Long transactionId
) {

}

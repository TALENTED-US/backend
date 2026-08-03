package com.talented.buttie.ledger.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Builder;

@ApiModel(description = "고정 지출 추가 요청")
@Builder
public record CreateFixedExpenseRequestDTO(
    @ApiModelProperty(value = "고정 지출로 전환할 거래 ID", example = "1", required = true)
    @NotNull(message = "거래 ID는 필수입니다.")
    Long transactionId
) {

}

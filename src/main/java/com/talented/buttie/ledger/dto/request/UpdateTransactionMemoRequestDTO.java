package com.talented.buttie.ledger.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Size;

@ApiModel(description = "외부 거래 메모 수정 요청 (외부거래 ID가 있을 때 사용)")
public record UpdateTransactionMemoRequestDTO(
    @ApiModelProperty(
        value = "수정할 메모 내용",
        example = "학식당에서 스팸치즈순두부찌개",
        required = false
    )
    @Size(max = 255, message = "메모는 최대 255자까지 입력 가능합니다.")
    String memo
) {

}
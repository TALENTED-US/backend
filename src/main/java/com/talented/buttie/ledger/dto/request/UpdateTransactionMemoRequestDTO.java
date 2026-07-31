package com.talented.buttie.ledger.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Size;

@ApiModel(description = "거래 메모 단일 수정 요청")
public record UpdateTransactionMemoRequestDTO(
    @ApiModelProperty(
        value = "수정할 메모 내용",
        example = "학식당에서 스팸치즈순두부찌개",
        required = false
    )
    @Size(max = 255, message = "메모는 최대 255자까지 입력 가능합니다.")
    String memo
){

}
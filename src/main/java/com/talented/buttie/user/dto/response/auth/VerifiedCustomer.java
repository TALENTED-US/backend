package com.talented.buttie.user.dto.response.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;

@Builder
@ApiModel(description = "인증된 고객 정보 DTO")
public record VerifiedCustomer(
    @ApiModelProperty(value = "사용자 이름", example = "홍길동")
    String name,
    @ApiModelProperty(value = "생년월일", example = "1990-01-01")
    String birthDate,
    @ApiModelProperty(value = "전화번호", example = "010-1234-5678")
    String phoneNumber
) {

}
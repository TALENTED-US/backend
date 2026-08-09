package com.talented.buttie.user.dto.response.auth;

import io.swagger.annotations.ApiModelProperty;

public record AuthVerifyResponse(
    @ApiModelProperty(value = "토큰", example = "your_token_here")
    String token,

    @ApiModelProperty(value = "인증된 정보")
    VerifiedCustomer verifiedCustomer
) {

}

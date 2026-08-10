package com.talented.buttie.user.dto.response.auth;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;

@Builder
public record AuthEmailResponse(

    @ApiModelProperty(value = "사용자 이메일")
    String email
) {

}

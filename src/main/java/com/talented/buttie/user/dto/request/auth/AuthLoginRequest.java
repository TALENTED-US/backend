package com.talented.buttie.user.dto.request.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("로그인 요청 DTO")
public record AuthLoginRequest(

    @ApiModelProperty(value = "사용자 이메일", example = "gildong@example.com")
    String userEmail,

    @ApiModelProperty(value = "비밀번호", example = "Sun1!2026")
    String password
) {
}

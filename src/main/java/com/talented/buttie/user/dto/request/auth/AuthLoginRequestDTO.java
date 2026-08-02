package com.talented.buttie.user.dto.request.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "로그인 요청 DTO")
public record AuthLoginRequestDTO(

    @ApiModelProperty(value = "사용자 이메일", example = "abc@example.com")
    String userEmail,

    @ApiModelProperty(value = "비밀번호", example = "Password123!")
    String password
) {
}

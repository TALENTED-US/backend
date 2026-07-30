package com.talented.buttie.user.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@ApiModel(description = "회원 탈퇴 요청 DTO")
public class withdrawUserRequestDTO {

    @ApiModelProperty(value = "비밀번호", example = "password1234", required = true)
    @NotNull(message = "비밀번호는 필수입니다.")
    String password;
}

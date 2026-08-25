package com.talented.buttie.user.dto.request.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;

@ApiModel(description = "회원 탈퇴 요청 DTO")
public record WithdrawUserRequest(

    @ApiModelProperty(value = "비밀번호", example = "password1234", required = true)
    @NotBlank(message = "비밀번호는 필수입니다.")
    String password
 ) {

}

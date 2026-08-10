package com.talented.buttie.user.dto.request.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@ApiModel("로그인 요청 DTO")
public record AuthPasswordRequest(

    @NotBlank(message = "비밀번호 입력은 필수입니다.")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,20}$",
        message = "비밀번호는 영문, 숫자, 특수문자를 포함한 8~20자여야 합니다."
    )
    @ApiModelProperty(value = "사용자 비밀번호", example = "Password123!")
    String password,

    @ApiModelProperty(value = "사용자 비밀번호 확인", example = "Password123!")
    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    String passwordCheck
) {

}

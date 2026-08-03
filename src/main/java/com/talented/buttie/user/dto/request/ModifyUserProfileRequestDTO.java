package com.talented.buttie.user.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

@ApiModel(description = "회원 프로필 수정 요청 DTO")
public record ModifyUserProfileRequestDTO(

    @ApiModelProperty(value = "닉네임", example = "재준이", required = true)
    @NotNull(message = "닉네임은 필수입니다.")
    String nickname

) {
}
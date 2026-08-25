package com.talented.buttie.user.dto.response.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "토큰 응답 DTO")
public record AuthTokenResponse(
    @ApiModelProperty(value = "액세스 토큰")
    String accessToken,
    @JsonIgnore @ApiModelProperty(value = "리프레시 토큰")
    String refreshToken,
    @JsonIgnore @ApiModelProperty(value = "리프레시 토큰 만료 시간")
    long refreshTokenExpiration
) {

}

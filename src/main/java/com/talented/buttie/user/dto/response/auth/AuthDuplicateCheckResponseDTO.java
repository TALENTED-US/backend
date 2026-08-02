package com.talented.buttie.user.dto.response.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;

@Builder
@ApiModel(description = "중복 체크 응답 DTO")
public record AuthDuplicateCheckResponseDTO(
    @ApiModelProperty(value = "중복 여부", example = "true")
    boolean isDuplicate
) {

}

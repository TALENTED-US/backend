package com.talented.buttie.notification.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "미확인 알림 존재 여부 조회 응답")
public record UnreadNotificationCheckResponse(
    @ApiModelProperty(value = "미확인 알림 존재 여부", example = "true", required = true)
    boolean hasUnread
) {
    public static UnreadNotificationCheckResponse of(boolean hasUnread) {

        return new UnreadNotificationCheckResponse(hasUnread);
    }
}

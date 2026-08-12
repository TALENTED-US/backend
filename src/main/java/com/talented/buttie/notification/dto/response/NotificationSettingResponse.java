package com.talented.buttie.notification.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "알림 수신 설정 조회 응답")
public record NotificationSettingResponse (
    @ApiModelProperty(value="정책 마감 알림 수신 여부", example = "true", required = true)
    boolean policyDeadlineEnabled,

    @ApiModelProperty(value="재정 변화 알림 수신 여부", example = "true", required = true)
    boolean financialChangeEnabled,

    @ApiModelProperty(value="계획 이탈 알림 수신 여부", example = "true", required = true)
    boolean planDeviationEnabled,

    @ApiModelProperty(value="서비스 공지 알림 수신 여부", example = "true", required = true)
    boolean serviceNoticeEnabled
) {
    public static NotificationSettingResponse of(
            boolean policy, boolean financialChange, boolean planDeviation, boolean serviceNotice){
        return new NotificationSettingResponse(policy, financialChange, planDeviation, serviceNotice);
    }

}


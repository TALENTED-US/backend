package com.talented.buttie.notification.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

@ApiModel(description = "알림 수신 설정 변경 요청")
public record UpdateNotificationSettingRequest(

    @ApiModelProperty(value = "정책 마감 알림 수신 여부", example = "true", required = true)
    @NotNull(message = "정책 마감 알림 수신 여부는 필수입니다.")
    Boolean policyDeadlineEnabled,

    @ApiModelProperty(value = "재정 변화 알림 수신 여부", example = "true", required = true)
    @NotNull(message = "재정 변화 알림 수신 여부는 필수입니다.")
    Boolean financialChangeEnabled,

    @ApiModelProperty(value = "계획 이탈 알림 수신 여부", example = "false", required = true)
    @NotNull(message = "계획 이탈 알림 수신 여부는 필수입니다.")
    Boolean planDeviationEnabled,

    @ApiModelProperty(value = "서비스 공지 알림 수신 여부", example = "true", required = true)
    @NotNull(message = "서비스 공지 알림 수신 여부는 필수입니다.")
    Boolean serviceNoticeEnabled
) {}

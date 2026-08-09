package com.talented.buttie.notification.dto.response;

import com.talented.buttie.notification.domain.NotificationListVO;
import com.talented.buttie.notification.domain.NotificationType;
import com.talented.buttie.common.util.PKCrypto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.List;

@ApiModel(description = "알림 목록 조회 응답")
public record GetNotificationListResponse(
    @ApiModelProperty(value="읽지 않은 알림 수",example="3",required = true)
    int unreadCount,

    @ApiModelProperty(value="알림 목록",required = true)
    List<NotificationSummary> notifications
) {
    @ApiModel(description = "알림 요약 정보")
    public record NotificationSummary(
        @ApiModelProperty(value="암호화된 알림 ID",example="pocketmon123go",required = true)
        String notificationId,

        @ApiModelProperty(value="알림 유형",example="QUEST",required = true)
        NotificationType notificationType,

        @ApiModelProperty(value="알림 제목",example="행동 과제 마감 임박",required = true)
        String notificationTitle,

        @ApiModelProperty(value="알림 내용",example="행동 과제 과제 마감이 4일 남았어요.",required = true)
        String notificationContent,

        @ApiModelProperty(value="읽음 여부",example="false",required = true)
        Boolean isRead,

        @ApiModelProperty(value="알림 생성 시간",example="2026-08-07T10:30:00",required = true)
        LocalDateTime notificationCreatedAt,

        @ApiModelProperty(value = "이동 URL", example = "/dashboard", required = false)
        String notificationUrl
    ) {}

    public static GetNotificationListResponse from(NotificationListVO vo) {
        return new GetNotificationListResponse(
            vo.getUnreadCount(),
            vo.getNotifications().stream()
                .map(n -> new NotificationSummary(
                    PKCrypto.encrypt(n.getNotificationId()),
                    n.getNotificationType(),
                    n.getNotificationTitle(),
                    n.getNotificationContent(),
                    n.getIsRead(),
                    n.getNotificationCreatedAt(),
                    n.getNotificationUrl()
                ))
                .toList()
        );
    }
}
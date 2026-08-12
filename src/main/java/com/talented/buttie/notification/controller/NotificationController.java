package com.talented.buttie.notification.controller;

import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.common.security.AuthenticationUser;
import com.talented.buttie.common.security.annotation.AuthUser;
import com.talented.buttie.notification.domain.NotificationListVO;
import com.talented.buttie.notification.dto.response.GetNotificationListResponse;
import com.talented.buttie.notification.dto.response.UnreadNotificationCheckResponse;
import com.talented.buttie.notification.service.NotificationService;
import com.talented.buttie.notification.dto.response.NotificationSettingResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@Api(tags = "Notification")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    @ApiOperation("알람 목록 조회")
    @GetMapping
    public ApplicationResponse<GetNotificationListResponse> getNotificationList(
        @AuthUser AuthenticationUser authUser,
        @RequestParam(defaultValue = "false") boolean unreadOnly
    ) {
        NotificationListVO vo = notificationService.getNotificationList(authUser.userId(), unreadOnly);
        return ApplicationResponse.onSuccess(GetNotificationListResponse.from(vo));
    }

    @ApiOperation("미확인 알림 존재 여부 조회")
    @GetMapping("/unread-check")
    public ApplicationResponse<UnreadNotificationCheckResponse> checkUnreadNotification(
        @AuthUser AuthenticationUser authUser
    ) {
        boolean hasUnread = notificationService.hasUnreadNotification(authUser.userId());
        return ApplicationResponse.onSuccess(UnreadNotificationCheckResponse.of(hasUnread));
    }

    @ApiOperation("알림 수신 설정 조회")
    @GetMapping("/settings")
    public ApplicationResponse<NotificationSettingResponse> getNotificationSettings(
        @AuthUser AuthenticationUser authUser
    ){
        NotificationSettingResponse response = notificationService.getNotificationSettings(authUser.userId());

        return ApplicationResponse.onSuccess(response);
    }

    @ApiOperation("알림 전체 읽음 처리")
    @PatchMapping("/read-all")
    public ApplicationResponse<Void> modifyAllNotificationsRead(
        @AuthUser AuthenticationUser authUser
    ) {
        notificationService.modifyAllNotificationsRead(authUser.userId());
        return ApplicationResponse.onSuccess(null);
    }
}

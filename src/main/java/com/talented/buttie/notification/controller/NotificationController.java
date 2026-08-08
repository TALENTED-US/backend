package com.talented.buttie.notification.controller;

import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.common.security.AuthenticationUser;
import com.talented.buttie.common.security.annotation.AuthUser;
import com.talented.buttie.notification.domain.NotificationListVO;
import com.talented.buttie.notification.dto.response.GetNotificationListResponse;
import com.talented.buttie.notification.service.NotificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@Api(tags = "Notification")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public ApplicationResponse<GetNotificationListResponse> getNotificationList(
        @AuthUser AuthenticationUser authUser,
        @RequestParam(defaultValue = "false") boolean unreadOnly
    ) {
        NotificationListVO vo = notificationService.getNotificationList(authUser.userId(), unreadOnly);
        return ApplicationResponse.onSuccess(GetNotificationListResponse.from(vo));
    }
}

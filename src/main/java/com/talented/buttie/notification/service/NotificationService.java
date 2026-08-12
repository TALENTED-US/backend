package com.talented.buttie.notification.service;

import com.talented.buttie.notification.domain.NotificationListVO;
import com.talented.buttie.notification.domain.NotificationVO;
import com.talented.buttie.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.talented.buttie.notification.domain.UserNotificationVO;
import com.talented.buttie.notification.dto.response.NotificationSettingResponse;

import java.util.List;
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationMapper notificationMapper;

    public NotificationListVO getNotificationList(Long userId, boolean unreadOnly) {
        List<NotificationVO> notifications = notificationMapper.selectNotificationList(userId, unreadOnly);
        int unreadCount = notificationMapper.selectUnreadCount(userId);

        return NotificationListVO.createNotificationList(unreadCount, notifications);
    }

    public boolean hasUnreadNotification(Long userId) {
        return notificationMapper.existsUnreadNotification(userId);
    }

    public NotificationSettingResponse getNotificationSettings(Long userId) {
        UserNotificationVO setting = notificationMapper.selectUserNotification(userId);

        if (setting == null) {
            return NotificationSettingResponse.of(true, true, true, true);
        }

        return NotificationSettingResponse.of(
            setting.getPolicyDeadlineNotificationYn(),
            setting.getFinancialChangeNotificationYn(),
            setting.getPlanDeviationNotificationYn(),
            setting.getServiceNoticeNotificationYn()
        );
    }
    public Long modifyAllNotificationsRead(Long userId) {
        notificationMapper.updateAllNotificationsRead(userId);
        return userId;
    }

}

package com.talented.buttie.notification.service;

import com.talented.buttie.notification.domain.NotificationListVO;
import com.talented.buttie.notification.domain.NotificationVO;
import com.talented.buttie.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}

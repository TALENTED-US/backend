package com.talented.buttie.notification.domain;
import lombok.Getter;

import java.util.List;

@Getter
public class NotificationListVO {
    private int unreadCount;
    private List<NotificationVO> notifications;

    public static NotificationListVO createNotificationList(int unreadCount, List<NotificationVO> notifications) {
        NotificationListVO vo = new NotificationListVO();
        vo.unreadCount = unreadCount;
        vo.notifications = notifications;
        return vo;
    }
}

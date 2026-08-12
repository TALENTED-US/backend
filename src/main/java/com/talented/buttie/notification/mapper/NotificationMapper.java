package com.talented.buttie.notification.mapper;

import com.talented.buttie.notification.domain.NotificationVO;
import com.talented.buttie.notification.domain.UserNotificationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {

    List<NotificationVO> selectNotificationList(
        @Param("userId") Long userId,
        @Param("unreadOnly") boolean unreadOnly
    );
    int selectUnreadCount(@Param("userId") Long userId);

    boolean existsUnreadNotification(@Param("userId") Long userId);

    Long selectUserIdByNotificationId(@Param("notificationId") Long notificationId);

    int updateNotificationRead(@Param("notificationId") Long notificationId);

    UserNotificationVO selectUserNotification(@Param("userId") Long userId);

    int upsertUserNotification(UserNotificationVO userNotificationVO);
}

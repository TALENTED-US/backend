package com.talented.buttie.notification.mapper;

import com.talented.buttie.notification.domain.NotificationVO;
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
}

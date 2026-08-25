package com.talented.buttie.notification.domain;

import java.time.LocalDateTime;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationVO {
    private Long notificationId;
    private Long userId;
    private NotificationType notificationType;
    private String notificationTitle;
    private String notificationContent;
    private String notificationUrl;
    private Boolean isRead;
    private LocalDateTime notificationCreatedAt;
}

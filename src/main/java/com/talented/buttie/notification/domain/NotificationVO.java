package com.talented.buttie.notification.domain;

import java.time.LocalDateTime;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationVO {
    private Long notificationId;
    private Long userId;
    private NotificationType type;
    private String title;
    private String content;
    private String url;
    private Boolean isRead;
    private LocalDateTime createdAt;
}

package com.talented.buttie.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotificationVO {
    private Long userId;
    private Boolean policyDeadlineNotificationYn;
    private Boolean financialChangeNotificationYn;
    private Boolean planDeviationNotificationYn;
    private Boolean serviceNoticeNotificationYn;
}

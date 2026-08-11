package com.talented.buttie.notification.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.talented.buttie.notification.mapper.NotificationMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    @DisplayName("미확인 알림이 있으면 true 반환")
    void hasUnreadNotificationReturnsTrue() {
        Long userId = 1L;
        given(notificationMapper.existsUnreadNotification(userId)).willReturn(true);

        boolean result = notificationService.hasUnreadNotification(userId);

        assertTrue(result);
        verify(notificationMapper).existsUnreadNotification(userId);
    }

    @Test
    @DisplayName("미확인 알림이 없으면 false 반환")
    void hasUnreadNotificationReturnsFalse() {
        Long userId = 1L;
        given(notificationMapper.existsUnreadNotification(userId)).willReturn(false);

        boolean result = notificationService.hasUnreadNotification(userId);

        assertFalse(result);
        verify(notificationMapper).existsUnreadNotification(userId);
    }
}

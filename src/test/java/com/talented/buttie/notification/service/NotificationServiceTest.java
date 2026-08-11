package com.talented.buttie.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.notification.domain.UserNotificationVO;
import com.talented.buttie.notification.dto.response.NotificationSettingResponse;
import com.talented.buttie.notification.exception.NotificationErrorCode;
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

    @Test
    @DisplayName("알림 수신 설정을 조회하면 설정 값을 반환한다")
    void getNotificationSettings() {
        Long userId = 1L;
        UserNotificationVO mockVO = new UserNotificationVO(
            userId, true, true, false, true
        );

        given(notificationMapper.selectUserNotification(userId)).willReturn(mockVO);

        NotificationSettingResponse result = notificationService.getNotificationSettings(userId);

        assertTrue(result.policyDeadlineEnabled());
        assertTrue(result.financialChangeEnabled());
        assertFalse(result.planDeviationEnabled());
        assertTrue(result.serviceNoticeEnabled());
        verify(notificationMapper).selectUserNotification(userId);
    }

    @Test
    @DisplayName("알림 수신 설정이 없으면 전체 true 기본값을 반환한다")
    void getNotificationSettingsReturnsDefaultWhenNull() {
        Long userId = 1L;

        given(notificationMapper.selectUserNotification(userId)).willReturn(null);

        NotificationSettingResponse result = notificationService.getNotificationSettings(userId);

        assertTrue(result.policyDeadlineEnabled());
        assertTrue(result.financialChangeEnabled());
        assertTrue(result.planDeviationEnabled());
        assertTrue(result.serviceNoticeEnabled());
        verify(notificationMapper).selectUserNotification(userId);
    }

    @Test
    @DisplayName("알림 개별 읽음 처리 시 정상적으로 notificationId를 반환한다")
    void modifyNotificationRead() {
        Long userId = 1L;
        Long notificationId = 10L;

        given(notificationMapper.selectUserIdByNotificationId(notificationId)).willReturn(userId);
        given(notificationMapper.updateNotificationRead(notificationId)).willReturn(1);

        Long result = notificationService.modifyNotificationRead(userId, notificationId);

        assertEquals(notificationId, result);
        verify(notificationMapper).updateNotificationRead(notificationId);
    }

    @Test
    @DisplayName("알림 개별 읽음 처리 시 알림이 없으면 예외가 발생한다")
    void throwWhenNotificationNotFound() {
        Long userId = 1L;
        Long notificationId = 999L;

        given(notificationMapper.selectUserIdByNotificationId(notificationId)).willReturn(null);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> notificationService.modifyNotificationRead(userId, notificationId)
        );

        assertEquals(NotificationErrorCode.NOTIFICATION_NOT_FOUND, exception.getCode());
        verify(notificationMapper, never()).updateNotificationRead(notificationId);
    }

    @Test
    @DisplayName("알림 개별 읽음 처리 시 타인 알림이면 예외가 발생한다")
    void throwWhenNotificationAccessDenied() {
        Long userId = 1L;
        Long notificationId = 10L;
        Long otherUserId = 2L;

        given(notificationMapper.selectUserIdByNotificationId(notificationId)).willReturn(otherUserId);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> notificationService.modifyNotificationRead(userId, notificationId)
        );

        assertEquals(NotificationErrorCode.NOTIFICATION_ACCESS_DENIED, exception.getCode());
        verify(notificationMapper, never()).updateNotificationRead(notificationId);
    }
}

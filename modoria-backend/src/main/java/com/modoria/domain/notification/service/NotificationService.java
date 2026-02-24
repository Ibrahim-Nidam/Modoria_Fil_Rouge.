package com.modoria.domain.notification.service;

import com.modoria.domain.notification.dto.response.NotificationResponse;
import com.modoria.domain.notification.enums.NotificationType;
import com.modoria.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    /**
     * Sends a notification to a specific user.
     * Persists the notification and broadcasts it via WebSocket.
     */
    void sendNotification(User user, String title, String content, NotificationType type, String linkUrl,
            String metadata);

    /**
     * Broadcasts a notification to all users with a specific role.
     */
    void notifyRole(String roleName, String title, String content, NotificationType type, String linkUrl,
            String metadata);

    Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable);

    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);

    long getUnreadCount(Long userId);
}

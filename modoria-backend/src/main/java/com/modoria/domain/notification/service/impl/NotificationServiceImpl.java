package com.modoria.domain.notification.service.impl;

import com.modoria.domain.notification.dto.response.NotificationResponse;
import com.modoria.domain.notification.entity.Notification;
import com.modoria.domain.notification.enums.NotificationType;
import com.modoria.domain.notification.repository.NotificationRepository;
import com.modoria.domain.notification.service.NotificationService;
import com.modoria.domain.user.entity.User;
import com.modoria.domain.user.repository.UserRepository;
import com.modoria.infrastructure.exceptions.resource.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendNotification(User user, String title, String content, NotificationType type, String linkUrl,
            String metadata) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .content(content)
                .type(type)
                .linkUrl(linkUrl)
                .metadata(metadata)
                .isRead(false)
                .build();

        notification = notificationRepository.save(notification);

        // Broadcast to user-specific WebSocket topic
        String destination = "/queue/notifications";
        NotificationResponse response = mapToResponse(notification);
        messagingTemplate.convertAndSendToUser(user.getEmail(), destination, response);

        log.info("Notification sent to user {}: {}", user.getEmail(), title);
    }

    @Override
    public void notifyRole(String roleName, String title, String content, NotificationType type, String linkUrl,
            String metadata) {
        List<User> users = userRepository.findAll().stream()
                .filter(u -> u.hasRole(roleName))
                .toList();

        for (User user : users) {
            sendNotification(user, title, content, type, linkUrl, metadata);
        }

        log.info("Notification sent to all {}s: {}", roleName, title);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (!notification.getUser().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Cannot mark other user's notification as read");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .content(n.getContent())
                .type(n.getType())
                .isRead(n.getIsRead())
                .linkUrl(n.getLinkUrl())
                .metadata(n.getMetadata())
                .createdAt(n.getCreatedAt())
                .build();
    }
}

package com.modoria.domain.notification;

import com.modoria.BaseIntegrationTest;
import com.modoria.domain.notification.entity.Notification;
import com.modoria.domain.notification.enums.NotificationType;
import com.modoria.domain.notification.repository.NotificationRepository;
import com.modoria.domain.notification.service.NotificationService;
import com.modoria.domain.user.entity.User;
import com.modoria.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class NotificationServiceTest extends BaseIntegrationTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    public void shouldSendAndRetrieveNotification() {
        // Given
        User user = userRepository.findByEmail("admin@modoria.com").orElseThrow();

        // When
        notificationService.sendNotification(user, "Test Title", "Test Content",
                NotificationType.ORDER_STATUS_UPDATE, "/test", "{}");

        // Then
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        assertThat(notifications).isNotEmpty();
        assertThat(notifications.get(0).getTitle()).isEqualTo("Test Title");
        assertThat(notifications.get(0).getContent()).isEqualTo("Test Content");
        assertThat(notifications.get(0).getIsRead()).isFalse();
    }
}

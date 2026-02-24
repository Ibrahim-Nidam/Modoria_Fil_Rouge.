package com.modoria.domain.notification.dto.response;

import com.modoria.domain.notification.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private String title;
    private String content;
    private NotificationType type;
    private Boolean isRead;
    private String linkUrl;
    private String metadata;
    private LocalDateTime createdAt;
}

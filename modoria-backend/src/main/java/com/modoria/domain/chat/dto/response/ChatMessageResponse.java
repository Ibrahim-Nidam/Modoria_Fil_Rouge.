package com.modoria.domain.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Chat message response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
        private Long id;
        private Long chatRoomId;
        private Long senderId;
        private String senderName;
        private String senderAvatar;
        private boolean fromSupport;
        private String content;
        private String messageType;
        private String attachmentUrl;
        private boolean read;
        private Double aiConfidence;
        private String senderType;
        private LocalDateTime createdAt;
}

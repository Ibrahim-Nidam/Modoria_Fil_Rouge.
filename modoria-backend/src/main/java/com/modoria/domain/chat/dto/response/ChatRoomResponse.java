package com.modoria.domain.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Chat room response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponse {
        private Long id;
        private Long customerId;
        private String customerName;
        private Long supportAgentId;
        private String supportAgentName;
        private String subject;
        private String status;
        private String priority;
        private int messageCount;
        private long unreadCount;
        private LocalDateTime lastMessageAt;
        private List<ChatMessageResponse> recentMessages;
        private String supportType;
        private Boolean humanEscalationRequested;
        private LocalDateTime createdAt;
}

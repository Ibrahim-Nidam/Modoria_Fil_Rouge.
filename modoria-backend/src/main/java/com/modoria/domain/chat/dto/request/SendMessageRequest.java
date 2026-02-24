package com.modoria.domain.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Send message request DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {
        @NotBlank(message = "Content is required")
        @Size(max = 2000, message = "Message must not exceed 2000 characters")
        private String content;

        private String messageType;

        private String attachmentUrl;
}


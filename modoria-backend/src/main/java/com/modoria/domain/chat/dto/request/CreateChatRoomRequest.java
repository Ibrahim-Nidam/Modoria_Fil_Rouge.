package com.modoria.domain.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create chat room request DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateChatRoomRequest {
        @NotBlank(message = "Subject is required")
        @Size(max = 255, message = "Subject must not exceed 255 characters")
        private String subject;

        private String priority;
}


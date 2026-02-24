package com.modoria.domain.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiResponse {
        private String response;
        private String model;
        private int tokensUsed;
        private long responseTimeMs;
}


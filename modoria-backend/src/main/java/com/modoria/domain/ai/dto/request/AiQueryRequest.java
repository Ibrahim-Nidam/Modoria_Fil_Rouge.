package com.modoria.domain.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI query request DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiQueryRequest {
        @NotBlank(message = "Query is required")
        @Size(max = 2000, message = "Query must not exceed 2000 characters")
        private String query;

        private String context;

        private Integer maxTokens;
}


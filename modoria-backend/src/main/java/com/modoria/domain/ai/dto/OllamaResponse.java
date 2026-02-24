package com.modoria.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO from Ollama API /api/generate endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OllamaResponse {

    @JsonProperty("model")
    private String model;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("response")
    private String response;

    @JsonProperty("done")
    private Boolean done;

    @JsonProperty("context")
    private int[] context;

    @JsonProperty("total_duration")
    private Long totalDuration;

    @JsonProperty("load_duration")
    private Long loadDuration;

    @JsonProperty("prompt_eval_duration")
    private Long promptEvalDuration;

    @JsonProperty("eval_duration")
    private Long evalDuration;
}

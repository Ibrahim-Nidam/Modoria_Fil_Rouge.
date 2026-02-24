package com.modoria.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Ollama API /api/generate endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OllamaRequest {

    @JsonProperty("model")
    private String model;

    @JsonProperty("prompt")
    private String prompt;

    @JsonProperty("stream")
    @Builder.Default
    private Boolean stream = false;

    @JsonProperty("options")
    private Options options;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Options {
        @JsonProperty("temperature")
        private Double temperature;

        @JsonProperty("num_predict")
        private Integer numPredict; // max tokens

        @JsonProperty("top_k")
        private Integer topK;

        @JsonProperty("top_p")
        private Double topP;
    }
}

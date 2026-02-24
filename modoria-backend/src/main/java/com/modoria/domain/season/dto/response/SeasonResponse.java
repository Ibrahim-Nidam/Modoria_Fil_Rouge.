package com.modoria.domain.season.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Month;

/**
 * Season response DTO.
 * Note: UI theming is handled by the frontend based on season name.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeasonResponse {
        private Long id;
        private String name;
        private String displayName;
        private String description;
        private Month startMonth;
        private Month endMonth;
        private boolean isActive;
        private boolean isCurrent;
}


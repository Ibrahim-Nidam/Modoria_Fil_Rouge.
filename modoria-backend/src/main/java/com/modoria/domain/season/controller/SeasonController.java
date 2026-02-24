package com.modoria.domain.season.controller;

import com.modoria.infrastructure.common.dto.ApiResponse;
import com.modoria.domain.season.dto.response.SeasonResponse;
import com.modoria.domain.season.service.SeasonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for season endpoints.
 */
@RestController
@RequestMapping("/api/v1/seasons")
@RequiredArgsConstructor
@Tag(name = "Seasons", description = "Season API")
@Slf4j
public class SeasonController {

    private final SeasonService seasonService;

    @GetMapping("/current")
    @Operation(summary = "Get current season")
    public ResponseEntity<ApiResponse<SeasonResponse>> getCurrent() {
        SeasonResponse response = seasonService.getCurrent();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get season by ID")
    public ResponseEntity<ApiResponse<SeasonResponse>> getById(@PathVariable Long id) {
        SeasonResponse response = seasonService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all seasons")
    public ResponseEntity<ApiResponse<List<SeasonResponse>>> getAll() {
        List<SeasonResponse> response = seasonService.getAll();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new season")
    public ResponseEntity<ApiResponse<SeasonResponse>> create(
            @RequestParam String name,
            @RequestParam String displayName,
            @RequestParam int startMonth,
            @RequestParam int endMonth) {
        log.info("Request to create season: {}", name);
        SeasonResponse response = seasonService.create(name, displayName, startMonth, endMonth);
        return ResponseEntity.ok(ApiResponse.success(response, "Season created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a season")
    public ResponseEntity<ApiResponse<SeasonResponse>> update(
            @PathVariable Long id,
            @RequestParam(required = false) String displayName,
            @RequestParam(required = false) String description) {
        SeasonResponse response = seasonService.update(id, displayName, description);
        return ResponseEntity.ok(ApiResponse.success(response, "Season updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a season")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        seasonService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Season deleted successfully"));
    }
}

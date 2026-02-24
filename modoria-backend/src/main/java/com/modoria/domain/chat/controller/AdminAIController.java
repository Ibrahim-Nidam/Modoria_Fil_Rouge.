package com.modoria.domain.chat.controller;

import com.modoria.domain.ai.service.AdminInsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/ai")
@RequiredArgsConstructor
public class AdminAIController {

    private final AdminInsightService adminInsightService;

    @GetMapping("/insights")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> getBusinessInsights() {
        String summary = adminInsightService.getBusinessSummary();
        return ResponseEntity.ok(Map.of("summary", summary));
    }

    @GetMapping("/inventory")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> getInventoryInsights() {
        String summary = adminInsightService.getInventoryInsights();
        return ResponseEntity.ok(Map.of("summary", summary));
    }
}

package com.modoria.domain.ai.controller;

import com.modoria.domain.ai.service.ProductKnowledgeService;
import com.modoria.infrastructure.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/ai/knowledge")
@RequiredArgsConstructor
@Tag(name = "AI Knowledge", description = "AI Knowledge Base management")
public class ProductKnowledgeController {

    private final ProductKnowledgeService productKnowledgeService;

    @PostMapping("/refresh")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Re-index all products into vector store (Admin)")
    public ResponseEntity<ApiResponse<Void>> refreshIndex() {
        productKnowledgeService.refreshIndex();
        return ResponseEntity.ok(ApiResponse.success(null, "Product re-indexing started asynchronously"));
    }
}

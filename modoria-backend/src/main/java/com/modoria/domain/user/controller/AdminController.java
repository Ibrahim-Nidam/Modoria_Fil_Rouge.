package com.modoria.domain.user.controller;

import com.modoria.domain.auth.dto.request.RegisterRequest;
import com.modoria.domain.auth.dto.response.AuthResponse;
import com.modoria.infrastructure.common.dto.ApiResponse;
import com.modoria.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.modoria.domain.auth.dto.request.RegisterRequest;
import com.modoria.domain.auth.dto.response.AuthResponse;
import com.modoria.infrastructure.common.dto.ApiResponse;
import com.modoria.domain.auth.service.AuthService;
import com.modoria.domain.admin.service.AdminService;
import com.modoria.domain.admin.dto.response.DashboardStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Administrator operations")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AuthService authService;
    private final AdminService adminService;

    @PostMapping("/users/support")
    @Operation(summary = "Create a new Support Agent user")
    public ResponseEntity<ApiResponse<AuthResponse>> createSupportUser(@Valid @RequestBody RegisterRequest request) {
        // We might need a specific method in AuthService to create users with specific
        // roles
        // or expose a method here.
        // Since AuthService.register defaults to CUSTOMER, we need a new method in
        // AuthService
        // OR we manually create the user here using UserService if available.
        // Let's assume we add a registerSupport method to AuthService for simplicity
        // and security.
        AuthResponse response = authService.registerSupport(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Support agent created successfully"));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard statistics")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }
}

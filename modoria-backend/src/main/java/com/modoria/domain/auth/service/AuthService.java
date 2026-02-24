package com.modoria.domain.auth.service;

import com.modoria.domain.auth.dto.request.LoginRequest;
import com.modoria.domain.auth.dto.request.RefreshTokenRequest;
import com.modoria.domain.auth.dto.request.RegisterRequest;
import com.modoria.domain.auth.dto.response.AuthResponse;

/**
 * Service interface for authentication operations.
 */
public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(String token);

    AuthResponse registerSupport(RegisterRequest request);

    void forgotPassword(String email);

    void resetPassword(String token, String newPassword);
}

package com.modoria.domain.auth.service.impl;

import com.modoria.domain.cart.entity.Cart;
import com.modoria.domain.user.entity.Role;
import com.modoria.domain.user.entity.User;
import com.modoria.domain.auth.dto.request.LoginRequest;
import com.modoria.domain.auth.dto.request.RefreshTokenRequest;
import com.modoria.domain.auth.dto.request.RegisterRequest;
import com.modoria.domain.auth.dto.response.AuthResponse;
import com.modoria.infrastructure.exceptions.auth.AuthenticationException;
import com.modoria.infrastructure.exceptions.auth.InvalidTokenException;
import com.modoria.infrastructure.exceptions.resource.ResourceAlreadyExistsException;
import com.modoria.infrastructure.exceptions.resource.ResourceNotFoundException;
import com.modoria.domain.user.mapper.UserMapper;
import com.modoria.domain.cart.repository.CartRepository;
import com.modoria.domain.user.repository.RoleRepository;
import com.modoria.domain.user.repository.UserRepository;
import com.modoria.infrastructure.security.UserPrincipal;
import com.modoria.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of AuthService for authentication operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final com.modoria.infrastructure.security.jwt.JwtTokenProvider tokenProvider;
    private final com.modoria.infrastructure.mail.EmailService emailService;
    private final UserMapper userMapper;
    private final com.modoria.domain.notification.service.NotificationService notificationService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw ResourceAlreadyExistsException.user(request.getEmail());
        }

        // Get default role (CUSTOMER)
        Role customerRole = roleRepository.findByName(Role.CUSTOMER)
                .orElseGet(() -> createDefaultRole(Role.CUSTOMER));

        // Create user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .isEnabled(true)
                .isEmailVerified(false)
                .isLocked(false)
                .build();

        user.addRole(customerRole);
        User savedUser = userRepository.save(user);

        // Send Welcome Email
        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFirstName());

        // Notify Admins
        notificationService.notifyRole("ROLE_ADMIN",
                "New User Registered",
                "A new user has registered: " + savedUser.getEmail(),
                com.modoria.domain.notification.enums.NotificationType.SYSTEM_ALERT,
                "/admin/users/" + savedUser.getId(),
                "{\"userId\":" + savedUser.getId() + "}");

        // Create empty cart for user
        Cart cart = Cart.builder()
                .user(savedUser)
                .currency("MAD")
                .build();
        cartRepository.save(cart);

        // Generate tokens
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String accessToken = tokenProvider.generateAccessToken(userPrincipal);
        String refreshToken = tokenProvider.generateRefreshToken(userPrincipal);

        log.info("User registered successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenExpiration())
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            String accessToken = tokenProvider.generateAccessToken(userPrincipal);
            String refreshToken = tokenProvider.generateRefreshToken(userPrincipal);

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> ResourceNotFoundException.user(request.getEmail()));

            // Reset failed login attempts
            if (user.getFailedLoginAttempts() > 0) {
                user.setFailedLoginAttempts(0);
                userRepository.save(user);
            }

            log.info("User logged in successfully: {}", request.getEmail());

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(tokenProvider.getAccessTokenExpiration())
                    .user(userMapper.toResponse(user))
                    .build();

        } catch (BadCredentialsException e) {
            handleFailedLogin(request.getEmail());
            throw AuthenticationException.invalidCredentials();
        } catch (DisabledException e) {
            throw AuthenticationException.accountDisabled();
        } catch (LockedException e) {
            throw AuthenticationException.accountLocked();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refreshing token");

        if (!tokenProvider.validateToken(request.getRefreshToken())) {
            throw InvalidTokenException.expired();
        }

        Long userId = tokenProvider.getUserIdFromToken(request.getRefreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.user(userId));

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String accessToken = tokenProvider.generateAccessToken(userPrincipal);
        String refreshToken = tokenProvider.generateRefreshToken(userPrincipal);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenExpiration())
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    public void logout(String token) {
        // In a stateless JWT implementation, logout is typically handled client-side
        // For enhanced security, you could implement a token blacklist using Redis
        log.info("User logged out");
    }

    private void handleFailedLogin(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);

            // Lock account after 5 failed attempts
            if (attempts >= 5) {
                user.setIsLocked(true);
                log.warn("Account locked due to too many failed login attempts: {}", email);
            }

            userRepository.save(user);
        });
    }

    private Role createDefaultRole(String roleName) {
        Role role = Role.builder()
                .name(roleName)
                .description("Default " + roleName.replace("ROLE_", "").toLowerCase() + " role")
                .isDefault(roleName.equals(Role.CUSTOMER))
                .isActive(true)
                .build();
        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public AuthResponse registerSupport(RegisterRequest request) {
        log.info("Registering new support agent with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw ResourceAlreadyExistsException.user(request.getEmail());
        }

        Role supportRole = roleRepository.findByName(Role.SUPPORT)
                .orElseGet(() -> createDefaultRole(Role.SUPPORT));

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .isEnabled(true)
                .isEmailVerified(true) // Support agents created by admin are verified by default
                .isLocked(false)
                .build();

        user.addRole(supportRole);
        user = userRepository.save(user);

        // Notify Admins
        notificationService.notifyRole("ROLE_ADMIN",
                "New Support Agent Created",
                "A new support agent has been created: " + user.getEmail(),
                com.modoria.domain.notification.enums.NotificationType.SYSTEM_ALERT,
                "/admin/users/" + user.getId(),
                "{\"userId\":" + user.getId() + "}");

        // Generate tokens
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String accessToken = tokenProvider.generateAccessToken(userPrincipal);
        String refreshToken = tokenProvider.generateRefreshToken(userPrincipal);

        log.info("Support agent registered successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenExpiration())
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        String token = java.util.UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpiry(java.time.LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        emailService.sendPasswordReset(user.getEmail(), token);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid reset token"));

        if (user.getResetPasswordTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
            throw new InvalidTokenException("Reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);
    }
}

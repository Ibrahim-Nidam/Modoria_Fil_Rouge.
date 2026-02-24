package com.modoria.domain.user.service.impl;

import com.modoria.domain.user.entity.Role;
import com.modoria.domain.user.entity.User;
import com.modoria.domain.user.dto.request.UpdateUserRequest;
import com.modoria.domain.user.dto.response.UserResponse;
import com.modoria.infrastructure.exceptions.resource.ResourceNotFoundException;
import com.modoria.domain.user.mapper.UserMapper;
import com.modoria.domain.user.repository.RoleRepository;
import com.modoria.domain.user.repository.UserRepository;
import com.modoria.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of UserService.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return getByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String search, Pageable pageable) {
        return userRepository.searchByNameOrEmail(search, pageable)
                .map(userMapper::toResponse);
    }

    @Override
    public UserResponse update(Long id, UpdateUserRequest request) {
        log.info("Updating user: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        updateUserFromRequest(user, request);
        log.info("User {} updated successfully", id);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse updateCurrentUser(UpdateUserRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Updating current user: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        updateUserFromRequest(user, request);
        log.info("Current user profile updated: {}", email);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting user: {}", id);
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        userRepository.deleteById(id);
        log.info("User {} deleted successfully", id);
    }

    @Override
    public void changePassword(Long id, String oldPassword, String newPassword) {
        log.info("Changing password for user: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.warn("Password change failed for user {} - incorrect old password", id);
            throw new IllegalArgumentException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed successfully for user: {}", id);
    }

    @Override
    public void assignRole(Long userId, Long roleId) {
        log.info("Assigning role {} to user {}", roleId, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        user.getRoles().add(role);
        userRepository.save(user);
        log.info("Role {} assigned to user {} successfully", role.getName(), userId);
    }

    @Override
    public void removeRole(Long userId, Long roleId) {
        log.info("Removing role {} from user {}", roleId, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        user.getRoles().remove(role);
        userRepository.save(user);
        log.info("Role {} removed from user {} successfully", role.getName(), userId);
    }

    @Override
    public void enableUser(Long id) {
        log.info("Enabling user: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setIsEnabled(true);
        userRepository.save(user);
        log.info("User {} enabled", id);
    }

    @Override
    public void disableUser(Long id) {
        log.info("Disabling user: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setIsEnabled(false);
        userRepository.save(user);
        log.warn("User {} disabled", id);
    }

    private void updateUserFromRequest(User user, UpdateUserRequest request) {
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
    }
}

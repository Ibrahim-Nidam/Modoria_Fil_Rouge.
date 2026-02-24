package com.modoria.domain.user.service;


import com.modoria.domain.user.dto.request.UpdateUserRequest;
import com.modoria.domain.user.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for user operations.
 */
public interface UserService {

    UserResponse getById(Long id);

    UserResponse getByEmail(String email);

    UserResponse getCurrentUser();

    Page<UserResponse> getAll(Pageable pageable);

    Page<UserResponse> searchUsers(String search, Pageable pageable);

    UserResponse update(Long id, UpdateUserRequest request);

    UserResponse updateCurrentUser(UpdateUserRequest request);

    void delete(Long id);

    void changePassword(Long id, String oldPassword, String newPassword);

    void assignRole(Long userId, Long roleId);

    void removeRole(Long userId, Long roleId);

    void enableUser(Long id);

    void disableUser(Long id);
}




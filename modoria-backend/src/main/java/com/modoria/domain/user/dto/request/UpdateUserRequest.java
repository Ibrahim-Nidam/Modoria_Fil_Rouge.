package com.modoria.domain.user.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Update user request DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
        @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
        private String firstName;

        @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
        private String lastName;

        @Email(message = "Invalid email format")
        private String email;

        private String phoneNumber;

        private String avatarUrl;
}



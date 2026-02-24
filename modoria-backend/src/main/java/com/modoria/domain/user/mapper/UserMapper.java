package com.modoria.domain.user.mapper;


import com.modoria.domain.user.entity.User;
import com.modoria.domain.user.entity.Role;
import com.modoria.domain.user.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Simple mapper for User entity.
 */
@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface UserMapper {

    @Mapping(target = "roles", source = "roles")
    UserResponse toResponse(User user);

    default Set<String> mapRoles(Set<Role> roles) {
        if (roles == null)
            return Set.of();
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}




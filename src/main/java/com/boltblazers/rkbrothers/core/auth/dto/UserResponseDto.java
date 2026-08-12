package com.boltblazers.rkbrothers.core.auth.dto;

import com.boltblazers.rkbrothers.core.auth.User;

import java.time.LocalDateTime;

public record UserResponseDto(
        Long id,
        String name,
        String phone,
        String role,
        boolean isActive,
        LocalDateTime createdAt
) {
    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getFullName(),
                user.getPhone(),
                user.getRole().name().toLowerCase(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }
}

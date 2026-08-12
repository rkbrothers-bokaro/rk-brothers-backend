package com.boltblazers.rkbrothers.core.auth.dto;

import com.boltblazers.rkbrothers.core.auth.Role;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UserUpdateRequestDto(
        String name,

        @Pattern(regexp = "[0-9]{10}", message = "Phone must be a 10-digit number")
        String phone,

        @NotNull(message = "Role is required")
        Role role,

        boolean enabled
) {
}

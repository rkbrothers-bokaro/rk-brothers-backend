package com.boltblazers.rkbrothers.core.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserCreateRequestDto(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "[0-9]{10}", message = "Phone must be 10 digits")
        String phone,

        // WRITE_ONLY: binds from the incoming request but is never
        // serialized back out — including into AuditAspect's Jackson-based
        // audit_log capture of method arguments.
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String password,

        @NotBlank(message = "Role is required")
        @Pattern(regexp = "admin|staff", message = "Role must be admin or staff")
        String role
) {
}

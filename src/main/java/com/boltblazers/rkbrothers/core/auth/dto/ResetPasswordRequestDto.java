package com.boltblazers.rkbrothers.core.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestDto(
        // WRITE_ONLY: see UserCreateRequestDto.password — prevents this
        // from ever being written into the AuditAspect's audit_log capture.
        @NotBlank(message = "New password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String newPassword
) {
}

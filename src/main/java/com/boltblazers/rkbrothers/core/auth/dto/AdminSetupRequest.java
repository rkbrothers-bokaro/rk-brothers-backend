package com.boltblazers.rkbrothers.core.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminSetupRequest(
        @NotBlank(message = "setupKey is required") String setupKey
) {
}

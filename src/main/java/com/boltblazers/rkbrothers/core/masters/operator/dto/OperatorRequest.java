package com.boltblazers.rkbrothers.core.masters.operator.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record OperatorRequest(
        @NotBlank(message = "Name is required") String name,

        @Pattern(regexp = "[0-9]{10}", message = "Phone must be a 10-digit number")
        String phone,

        @NotBlank(message = "Licence number is required") String licenceNo,

        @FutureOrPresent(message = "Licence expiry must be a future or present date")
        LocalDate licenceExpiry,

        String category
) {
}

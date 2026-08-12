package com.boltblazers.rkbrothers.core.masters.party.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PartyRequest(
        @NotBlank(message = "Name is required") String name,

        String contactPerson,

        @Pattern(regexp = "[0-9]{10}", message = "Phone must be a 10-digit number")
        String phone,

        @Email(message = "Email must be valid")
        String email
) {
}

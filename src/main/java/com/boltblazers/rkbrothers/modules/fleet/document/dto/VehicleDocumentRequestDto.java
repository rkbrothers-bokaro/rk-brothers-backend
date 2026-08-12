package com.boltblazers.rkbrothers.modules.fleet.document.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record VehicleDocumentRequestDto(
        @NotNull(message = "Vehicle is required")
        Long vehicleId,

        @Pattern(regexp = "insurance|gate_pass|puc|fitness|tax|state_permit|other",
                message = "Document type must be one of insurance, gate_pass, puc, fitness, tax, state_permit, other")
        String documentType,

        String documentNo,

        LocalDate issuedDate,

        @NotNull(message = "Expiry date is required")
        LocalDate expiryDate
) {
}

package com.boltblazers.rkbrothers.modules.fleet.document.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record VehicleDocumentRequestDto(
        Long vehicleId,

        String documentType,

        String documentNo,

        LocalDate issuedDate,

        @NotNull(message = "Expiry date is required")
        LocalDate expiryDate
) {
}

package com.boltblazers.rkbrothers.modules.fleet.tipper.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TipperHrEntryDto(
        @NotNull(message = "Opening hours is required")
        BigDecimal openingHrs,

        @NotNull(message = "Closing hours is required")
        BigDecimal closingHrs,

        BigDecimal totalHrs
) {
}

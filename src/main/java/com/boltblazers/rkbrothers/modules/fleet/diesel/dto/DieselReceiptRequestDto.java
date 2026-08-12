package com.boltblazers.rkbrothers.modules.fleet.diesel.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DieselReceiptRequestDto(
        @NotNull(message = "Date is required")
        @PastOrPresent(message = "Date cannot be in the future")
        LocalDate date,

        @NotNull(message = "Vehicle is required")
        Long vehicleId,

        @NotNull(message = "Litres is required")
        @Positive(message = "Litres must be positive")
        BigDecimal litres,

        String receivedFrom,

        String invoiceNo
) {
}

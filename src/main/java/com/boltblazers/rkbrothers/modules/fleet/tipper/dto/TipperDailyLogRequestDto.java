package com.boltblazers.rkbrothers.modules.fleet.tipper.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TipperDailyLogRequestDto(
        @NotNull(message = "Date is required")
        @PastOrPresent(message = "Date cannot be in the future")
        LocalDate date,

        @NotNull(message = "Vehicle is required")
        Long vehicleId,

        Long workOrderId,

        Long driverId,

        @NotNull(message = "Opening hours is required")
        BigDecimal openingHrs,

        @NotNull(message = "Closing hours is required")
        BigDecimal closingHrs,

        @NotNull(message = "Opening km is required")
        BigDecimal openingKm,

        @NotNull(message = "Closing km is required")
        BigDecimal closingKm,

        @Positive(message = "Diesel litres must be positive")
        BigDecimal dieselLtr,

        BigDecimal dieselHrs,

        BigDecimal dieselKm,

        BigDecimal runKm,

        Integer tripCount,

        String workDescription,

        List<@Valid TipperTripEntryDto> tripEntries,

        List<@Valid TipperHrEntryDto> hrEntries
) {
}

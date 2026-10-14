package com.boltblazers.rkbrothers.modules.fleet.tipper.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;

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

        BigDecimal openingHrs,

        BigDecimal closingHrs,

        BigDecimal openingKm,

        BigDecimal closingKm,

        @PositiveOrZero(message = "Diesel litres must be positive or zero")
        BigDecimal dieselLtr,

        BigDecimal dieselHrs,

        BigDecimal dieselKm,

        BigDecimal runKm,

        Integer tripCount,

        String workDescription,

        String receivingSlipUrl,

        Boolean noWorkDone,

        List<@Valid TipperTripEntryDto> tripEntries,

        List<@Valid TipperHrEntryDto> hrEntries,

        String partyName,

        Boolean driverAbsent
) {
}

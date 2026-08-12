package com.boltblazers.rkbrothers.modules.fleet.jcb.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record JcbDailyLogRequestDto(
        @NotNull(message = "Date is required")
        @PastOrPresent(message = "Date cannot be in the future")
        LocalDate date,

        @NotNull(message = "Vehicle is required")
        Long vehicleId,

        Long workOrderId,

        Long operatorId,

        @NotNull(message = "Opening hours is required")
        BigDecimal openingHrs,

        @NotNull(message = "Closing hours is required")
        BigDecimal closingHrs,

        @Positive(message = "Diesel litres must be positive")
        BigDecimal dieselLtr,

        BigDecimal dieselMtr,

        BigDecimal runningLtr,

        String materialType,

        BigDecimal materialQty,

        String materialUnit,

        String workDescription,

        @NotEmpty(message = "At least one shift is required")
        List<@Valid JcbShiftDto> shifts
) {
}

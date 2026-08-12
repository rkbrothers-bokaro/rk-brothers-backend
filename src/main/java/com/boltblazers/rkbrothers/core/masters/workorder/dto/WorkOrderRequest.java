package com.boltblazers.rkbrothers.core.masters.workorder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WorkOrderRequest(
        @NotBlank(message = "Work order number is required")
        @Size(max = 50, message = "Work order number must be at most 50 characters")
        String woNumber,

        Long partyId,

        String description,

        String siteLocation,

        @Pattern(regexp = "hour|trip|km|railline", message = "Billing basis must be one of hour, trip, km, railline")
        String billingBasis,

        @Positive(message = "Rate must be positive")
        BigDecimal rate,

        String unit,

        LocalDate startDate,

        LocalDate endDate
) {
}

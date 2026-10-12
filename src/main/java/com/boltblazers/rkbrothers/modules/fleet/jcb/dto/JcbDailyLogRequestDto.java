package com.boltblazers.rkbrothers.modules.fleet.jcb.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;

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

        BigDecimal openingHrs,

        BigDecimal closingHrs,

        @PositiveOrZero(message = "Diesel litres must be positive or zero")
        BigDecimal dieselLtr,

        BigDecimal dieselMtr,

        BigDecimal runningLtr,

        String materialType,

        BigDecimal materialQty,

        String materialUnit,

        String workDescription,

        String receivingSlipUrl,

        Boolean noWorkDone,

        List<@Valid JcbShiftDto> shifts
) {
}

package com.boltblazers.rkbrothers.modules.fleet.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BillEntryDto(
        LocalDate date,
        BigDecimal qty
) {
}

package com.boltblazers.rkbrothers.modules.fleet.diesel.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DieselStockRowDto(
        LocalDate date,
        Long vehicleId,
        String vehicleNo,
        String vehicleDisplayName,
        BigDecimal openingBalance,
        BigDecimal received,
        BigDecimal totalStock,
        BigDecimal hsdIssue,
        BigDecimal closingBalance,
        BigDecimal progressiveTotal,
        BigDecimal totalKm,
        BigDecimal avgLtrPerHr,
        BigDecimal avgKmPerLtr
) {
}

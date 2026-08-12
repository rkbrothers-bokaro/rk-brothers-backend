package com.boltblazers.rkbrothers.modules.fleet.diesel.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FuelAnomalyDto(
        Long vehicleId,
        String vehicleNo,
        String vehicleDisplayName,
        BigDecimal todayAvg,
        BigDecimal rollingAvg,
        BigDecimal spikePercentage,
        LocalDate date,
        String type
) {
}

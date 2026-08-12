package com.boltblazers.rkbrothers.modules.fleet.dashboard.dto;

import java.math.BigDecimal;

public record DashboardSummaryDto(
        long vehiclesWorking,
        long vehiclesTotal,
        BigDecimal hoursLoggedToday,
        BigDecimal dieselInStock,
        int openAlertsCount,
        int documentsExpiringCount
) {
}

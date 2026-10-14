package com.boltblazers.rkbrothers.modules.fleet.billing.dto;

import java.math.BigDecimal;

public record BillablePeriodDto(
        Long workOrderId,
        String woNumber,
        String partyName,
        String siteLocation,
        String billingBasis,
        BigDecimal rate,
        String unit,
        int month,
        int year
) {
}

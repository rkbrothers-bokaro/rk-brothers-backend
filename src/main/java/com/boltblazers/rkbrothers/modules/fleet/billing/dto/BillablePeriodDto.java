package com.boltblazers.rkbrothers.modules.fleet.billing.dto;

public record BillablePeriodDto(
        Long workOrderId,
        String woNumber,
        int month,
        int year
) {
}

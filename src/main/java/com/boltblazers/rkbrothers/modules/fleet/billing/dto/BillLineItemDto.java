package com.boltblazers.rkbrothers.modules.fleet.billing.dto;

import java.math.BigDecimal;
import java.util.List;

public record BillLineItemDto(
        Long vehicleId,
        String vehicleNo,
        String vehicleDisplayName,
        String vehicleType,
        Long workOrderId,
        String woNumber,
        String partyName,
        String billingBasis,
        BigDecimal quantity,
        BigDecimal rate,
        BigDecimal amount,
        String unit,
        List<BillEntryDto> entries
) {
}

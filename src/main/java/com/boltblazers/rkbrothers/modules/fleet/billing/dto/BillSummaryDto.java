package com.boltblazers.rkbrothers.modules.fleet.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BillSummaryDto(
        Long workOrderId,
        String woNumber,
        Long partyId,
        String partyName,
        String siteLocation,
        int month,
        int year,
        List<BillLineItemDto> lineItems,
        BigDecimal totalAmount,
        LocalDateTime generatedAt
) {
}

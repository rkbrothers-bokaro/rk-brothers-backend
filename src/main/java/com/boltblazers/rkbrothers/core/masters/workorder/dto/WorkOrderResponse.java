package com.boltblazers.rkbrothers.core.masters.workorder.dto;

import com.boltblazers.rkbrothers.core.masters.workorder.WorkOrder;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WorkOrderResponse(
        Long id,
        String woNumber,
        Long partyId,
        String partyName,
        String description,
        String siteLocation,
        String billingBasis,
        BigDecimal rate,
        String unit,
        LocalDate startDate,
        LocalDate endDate
) {
    public static WorkOrderResponse from(WorkOrder workOrder) {
        return new WorkOrderResponse(
                workOrder.getId(),
                workOrder.getWoNumber(),
                workOrder.getParty() != null ? workOrder.getParty().getId() : null,
                workOrder.getParty() != null ? workOrder.getParty().getName() : null,
                workOrder.getDescription(),
                workOrder.getSiteLocation(),
                workOrder.getBillingBasis(),
                workOrder.getRate(),
                workOrder.getUnit(),
                workOrder.getStartDate(),
                workOrder.getEndDate()
        );
    }
}

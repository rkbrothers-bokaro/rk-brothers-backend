package com.boltblazers.rkbrothers.core.masters.vehicle.dto;

import com.boltblazers.rkbrothers.core.masters.vehicle.Vehicle;
import com.boltblazers.rkbrothers.core.masters.vehicle.VehicleStatus;

public record VehicleResponse(
        Long id,
        String vehicleNo,
        String displayName,
        String type,
        String billingBasis,
        Long assignedOperatorId,
        String assignedOperatorName,
        VehicleStatus status
) {
    public static VehicleResponse from(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getVehicleNo(),
                vehicle.getDisplayName(),
                vehicle.getType(),
                vehicle.getBillingBasis(),
                vehicle.getAssignedOperator() != null ? vehicle.getAssignedOperator().getId() : null,
                vehicle.getAssignedOperator() != null ? vehicle.getAssignedOperator().getName() : null,
                vehicle.getStatus()
        );
    }
}

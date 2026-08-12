package com.boltblazers.rkbrothers.core.masters.vehicle.dto;

import com.boltblazers.rkbrothers.core.masters.vehicle.VehicleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VehicleRequest(
        @NotBlank(message = "Vehicle number is required")
        @Size(max = 30, message = "Vehicle number must be at most 30 characters")
        String vehicleNo,

        String displayName,

        @NotBlank(message = "Type is required")
        String type,

        @Pattern(regexp = "hour|trip|km|railline", message = "Billing basis must be one of hour, trip, km, railline")
        String billingBasis,

        Long assignedOperatorId,

        @NotNull(message = "Status is required")
        VehicleStatus status
) {
}

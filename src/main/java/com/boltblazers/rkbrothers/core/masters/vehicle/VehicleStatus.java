package com.boltblazers.rkbrothers.core.masters.vehicle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum VehicleStatus {
    WORKING,
    IDLE,
    BREAKDOWN,
    INACTIVE;

    @JsonCreator
    public static VehicleStatus fromValue(String value) {
        for (VehicleStatus status : VehicleStatus.values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }

    @JsonValue
    public String getValue() {
        return name().toLowerCase();
    }
}

package com.boltblazers.rkbrothers.modules.fleet.tipper.dto;

public record TipperTripEntryDto(
        String fromLocation,
        String toLocation,
        Integer trips
) {
}

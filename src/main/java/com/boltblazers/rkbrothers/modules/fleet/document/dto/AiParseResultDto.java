package com.boltblazers.rkbrothers.modules.fleet.document.dto;

import java.time.LocalDate;

public record AiParseResultDto(
        String documentType,
        String documentNo,
        String vehicleNo,
        LocalDate issuedDate,
        LocalDate expiryDate,
        String confidence,
        String rawResponse
) {
    public static AiParseResultDto empty() {
        return new AiParseResultDto(null, null, null, null, null, "low", null);
    }
}

package com.boltblazers.rkbrothers.modules.fleet.document.dto;

import com.boltblazers.rkbrothers.modules.fleet.document.VehicleDocument;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record VehicleDocumentResponseDto(
        Long id,
        Long vehicleId,
        String vehicleNo,
        String vehicleDisplayName,
        String documentType,
        String documentNo,
        LocalDate issuedDate,
        LocalDate expiryDate,
        String documentUrl,
        String aiParsedData,
        LocalDateTime reminderSentAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        long daysUntilExpiry,
        String expiryStatus
) {
    public static VehicleDocumentResponseDto from(VehicleDocument doc) {
        long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDate.now(), doc.getExpiryDate());
        String expiryStatus;
        if (daysUntilExpiry > 30) {
            expiryStatus = "valid";
        } else if (daysUntilExpiry >= 1) {
            expiryStatus = "expiring_soon";
        } else {
            expiryStatus = "expired";
        }

        return new VehicleDocumentResponseDto(
                doc.getId(),
                doc.getVehicle() != null ? doc.getVehicle().getId() : null,
                doc.getVehicle() != null ? doc.getVehicle().getVehicleNo() : null,
                doc.getVehicle() != null ? doc.getVehicle().getDisplayName() : null,
                doc.getDocumentType(),
                doc.getDocumentNo(),
                doc.getIssuedDate(),
                doc.getExpiryDate(),
                doc.getDocumentUrl(),
                doc.getAiParsedData(),
                doc.getReminderSentAt(),
                doc.getCreatedAt(),
                doc.getUpdatedAt(),
                daysUntilExpiry,
                expiryStatus
        );
    }
}

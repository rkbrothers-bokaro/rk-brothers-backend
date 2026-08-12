package com.boltblazers.rkbrothers.modules.fleet.diesel.dto;

import com.boltblazers.rkbrothers.modules.fleet.diesel.DieselReceipt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record DieselReceiptResponseDto(
        Long id,
        LocalDate date,
        Long vehicleId,
        String vehicleNo,
        String vehicleDisplayName,
        BigDecimal litres,
        String receivedFrom,
        String invoiceNo,
        String slipUrl,
        Long enteredById,
        String enteredByName,
        LocalDateTime createdAt
) {
    public static DieselReceiptResponseDto from(DieselReceipt receipt) {
        return new DieselReceiptResponseDto(
                receipt.getId(),
                receipt.getDate(),
                receipt.getVehicle() != null ? receipt.getVehicle().getId() : null,
                receipt.getVehicle() != null ? receipt.getVehicle().getVehicleNo() : null,
                receipt.getVehicle() != null ? receipt.getVehicle().getDisplayName() : null,
                receipt.getLitres(),
                receipt.getReceivedFrom(),
                receipt.getInvoiceNo(),
                receipt.getSlipUrl(),
                receipt.getEnteredBy() != null ? receipt.getEnteredBy().getId() : null,
                receipt.getEnteredBy() != null ? receipt.getEnteredBy().getFullName() : null,
                receipt.getCreatedAt()
        );
    }
}

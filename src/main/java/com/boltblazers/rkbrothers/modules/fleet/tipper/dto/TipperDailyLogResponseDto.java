package com.boltblazers.rkbrothers.modules.fleet.tipper.dto;

import com.boltblazers.rkbrothers.modules.fleet.tipper.TipperDailyLog;
import com.boltblazers.rkbrothers.modules.fleet.tipper.TipperHrEntry;
import com.boltblazers.rkbrothers.modules.fleet.tipper.TipperTripEntry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TipperDailyLogResponseDto(
        Long id,
        LocalDate date,
        Long vehicleId,
        String vehicleNo,
        Long workOrderId,
        String workOrderNumber,
        Long driverId,
        String driverName,
        BigDecimal openingHrs,
        BigDecimal closingHrs,
        BigDecimal totalHrs,
        BigDecimal openingKm,
        BigDecimal closingKm,
        BigDecimal totalKm,
        BigDecimal dieselLtr,
        BigDecimal dieselHrs,
        BigDecimal dieselKm,
        BigDecimal runKm,
        BigDecimal avgKmPerLtr,
        Integer tripCount,
        String workDescription,
        String receivingSlipUrl,
        String status,
        Long submittedById,
        String submittedByName,
        List<TipperTripEntryDto> tripEntries,
        List<TipperHrEntryDto> hrEntries
) {
    public static TipperDailyLogResponseDto from(TipperDailyLog log) {
        return new TipperDailyLogResponseDto(
                log.getId(),
                log.getDate(),
                log.getVehicle() != null ? log.getVehicle().getId() : null,
                log.getVehicle() != null ? log.getVehicle().getVehicleNo() : null,
                log.getWorkOrder() != null ? log.getWorkOrder().getId() : null,
                log.getWorkOrder() != null ? log.getWorkOrder().getWoNumber() : null,
                log.getDriver() != null ? log.getDriver().getId() : null,
                log.getDriver() != null ? log.getDriver().getName() : null,
                log.getOpeningHrs(),
                log.getClosingHrs(),
                log.getTotalHrs(),
                log.getOpeningKm(),
                log.getClosingKm(),
                log.getTotalKm(),
                log.getDieselLtr(),
                log.getDieselHrs(),
                log.getDieselKm(),
                log.getRunKm(),
                log.getAvgKmPerLtr(),
                log.getTripCount(),
                log.getWorkDescription(),
                log.getReceivingSlipUrl(),
                log.getStatus(),
                log.getSubmittedBy() != null ? log.getSubmittedBy().getId() : null,
                log.getSubmittedBy() != null ? log.getSubmittedBy().getFullName() : null,
                log.getTripEntries().stream().map(TipperDailyLogResponseDto::tripToDto).toList(),
                log.getHrEntries().stream().map(TipperDailyLogResponseDto::hrToDto).toList()
        );
    }

    private static TipperTripEntryDto tripToDto(TipperTripEntry entry) {
        return new TipperTripEntryDto(entry.getFromLocation(), entry.getToLocation(), entry.getTrips());
    }

    private static TipperHrEntryDto hrToDto(TipperHrEntry entry) {
        return new TipperHrEntryDto(entry.getOpeningHrs(), entry.getClosingHrs(), entry.getTotalHrs());
    }
}

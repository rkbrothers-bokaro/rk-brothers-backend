package com.boltblazers.rkbrothers.modules.fleet.jcb.dto;

import com.boltblazers.rkbrothers.modules.fleet.jcb.JcbDailyLog;
import com.boltblazers.rkbrothers.modules.fleet.jcb.JcbDailyLogShift;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record JcbDailyLogResponseDto(
        Long id,
        LocalDate date,
        Long vehicleId,
        String vehicleNo,
        String vehicleDisplayName,
        Long workOrderId,
        String workOrderNumber,
        String workOrderDescription,
        Long operatorId,
        String operatorName,
        BigDecimal openingHrs,
        BigDecimal closingHrs,
        BigDecimal totalHrs,
        BigDecimal dieselLtr,
        BigDecimal dieselMtr,
        BigDecimal runningLtr,
        BigDecimal avgLtrPerHr,
        String materialType,
        BigDecimal materialQty,
        String materialUnit,
        String workDescription,
        String receivingSlipUrl,
        String status,
        Long submittedById,
        String submittedByName,
        List<JcbShiftDto> shifts
) {
    public static JcbDailyLogResponseDto from(JcbDailyLog log) {
        return new JcbDailyLogResponseDto(
                log.getId(),
                log.getDate(),
                log.getVehicle() != null ? log.getVehicle().getId() : null,
                log.getVehicle() != null ? log.getVehicle().getVehicleNo() : null,
                log.getVehicle() != null ? log.getVehicle().getDisplayName() : null,
                log.getWorkOrder() != null ? log.getWorkOrder().getId() : null,
                log.getWorkOrder() != null ? log.getWorkOrder().getWoNumber() : null,
                log.getWorkOrder() != null ? log.getWorkOrder().getDescription() : null,
                log.getOperator() != null ? log.getOperator().getId() : null,
                log.getOperator() != null ? log.getOperator().getName() : null,
                log.getOpeningHrs(),
                log.getClosingHrs(),
                log.getTotalHrs(),
                log.getDieselLtr(),
                log.getDieselMtr(),
                log.getRunningLtr(),
                log.getAvgLtrPerHr(),
                log.getMaterialType(),
                log.getMaterialQty(),
                log.getMaterialUnit(),
                log.getWorkDescription(),
                log.getReceivingSlipUrl(),
                log.getStatus(),
                log.getSubmittedBy() != null ? log.getSubmittedBy().getId() : null,
                log.getSubmittedBy() != null ? log.getSubmittedBy().getFullName() : null,
                log.getShifts().stream().map(JcbDailyLogResponseDto::shiftToDto).toList()
        );
    }

    private static JcbShiftDto shiftToDto(JcbDailyLogShift shift) {
        return new JcbShiftDto(shift.getStartTime(), shift.getCloseTime(), shift.getTotalTime());
    }
}

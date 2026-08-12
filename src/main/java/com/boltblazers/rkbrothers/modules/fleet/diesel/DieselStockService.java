package com.boltblazers.rkbrothers.modules.fleet.diesel;

import com.boltblazers.rkbrothers.core.common.ResourceNotFoundException;
import com.boltblazers.rkbrothers.core.masters.vehicle.Vehicle;
import com.boltblazers.rkbrothers.core.masters.vehicle.VehicleRepository;
import com.boltblazers.rkbrothers.modules.fleet.diesel.dto.DieselStockRowDto;
import com.boltblazers.rkbrothers.modules.fleet.jcb.JcbDailyLog;
import com.boltblazers.rkbrothers.modules.fleet.jcb.JcbDailyLogRepository;
import com.boltblazers.rkbrothers.modules.fleet.tipper.TipperDailyLog;
import com.boltblazers.rkbrothers.modules.fleet.tipper.TipperDailyLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DieselStockService {

    private final VehicleRepository vehicleRepository;
    private final DieselReceiptRepository dieselReceiptRepository;
    private final JcbDailyLogRepository jcbDailyLogRepository;
    private final TipperDailyLogRepository tipperDailyLogRepository;

    public List<DieselStockRowDto> getMonthlyRegister(int month, int year, Long vehicleId) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        List<Vehicle> vehicles = vehicleId != null
                ? List.of(vehicleRepository.findById(vehicleId)
                        .filter(Vehicle::isActive)
                        .orElseThrow(() -> ResourceNotFoundException.of("Vehicle", vehicleId)))
                : vehicleRepository.findAllByIsActiveTrue(Pageable.unpaged()).getContent();

        List<DieselStockRowDto> rows = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            rows.addAll(buildRowsForVehicle(vehicle, monthStart, monthEnd));
        }

        return rows.stream().sorted(Comparator.comparing(DieselStockRowDto::date)).toList();
    }

    private List<DieselStockRowDto> buildRowsForVehicle(Vehicle vehicle, LocalDate monthStart, LocalDate monthEnd) {
        BigDecimal openingBalance = computeOpeningBalance(vehicle.getId(), monthStart);

        Map<LocalDate, BigDecimal> receivedByDate = dieselReceiptRepository
                .findByVehicleIdAndDateBetween(vehicle.getId(), monthStart, monthEnd).stream()
                .collect(Collectors.groupingBy(DieselReceipt::getDate,
                        Collectors.reducing(BigDecimal.ZERO, DieselReceipt::getLitres, BigDecimal::add)));

        Map<LocalDate, JcbDailyLog> jcbByDate = jcbDailyLogRepository
                .findByVehicleIdAndDateBetween(vehicle.getId(), monthStart, monthEnd).stream()
                .collect(Collectors.toMap(JcbDailyLog::getDate, Function.identity(), (a, b) -> a));

        Map<LocalDate, TipperDailyLog> tipperByDate = tipperDailyLogRepository
                .findByVehicleIdAndDateBetween(vehicle.getId(), monthStart, monthEnd).stream()
                .collect(Collectors.toMap(TipperDailyLog::getDate, Function.identity(), (a, b) -> a));

        List<DieselStockRowDto> rows = new ArrayList<>();
        BigDecimal runningBalance = openingBalance;
        BigDecimal progressiveTotal = BigDecimal.ZERO;

        for (LocalDate day = monthStart; !day.isAfter(monthEnd); day = day.plusDays(1)) {
            BigDecimal received = receivedByDate.getOrDefault(day, BigDecimal.ZERO);
            JcbDailyLog jcbLog = jcbByDate.get(day);
            TipperDailyLog tipperLog = tipperByDate.get(day);

            BigDecimal hsdIssue = BigDecimal.ZERO;
            if (jcbLog != null && jcbLog.getDieselLtr() != null) {
                hsdIssue = hsdIssue.add(jcbLog.getDieselLtr());
            }
            if (tipperLog != null && tipperLog.getDieselLtr() != null) {
                hsdIssue = hsdIssue.add(tipperLog.getDieselLtr());
            }

            BigDecimal totalStock = runningBalance.add(received);
            BigDecimal closingBalance = totalStock.subtract(hsdIssue);
            progressiveTotal = progressiveTotal.add(received).subtract(hsdIssue);

            if (received.compareTo(BigDecimal.ZERO) > 0 || hsdIssue.compareTo(BigDecimal.ZERO) > 0) {
                rows.add(new DieselStockRowDto(
                        day,
                        vehicle.getId(),
                        vehicle.getVehicleNo(),
                        vehicle.getDisplayName(),
                        runningBalance,
                        received,
                        totalStock,
                        hsdIssue,
                        closingBalance,
                        progressiveTotal,
                        tipperLog != null ? tipperLog.getTotalKm() : null,
                        jcbLog != null ? jcbLog.getAvgLtrPerHr() : null,
                        tipperLog != null ? tipperLog.getAvgKmPerLtr() : null
                ));
            }

            runningBalance = closingBalance;
        }

        return rows;
    }

    private BigDecimal computeOpeningBalance(Long vehicleId, LocalDate monthStart) {
        BigDecimal receivedBefore = dieselReceiptRepository.sumLitresBefore(vehicleId, monthStart);
        BigDecimal jcbBefore = jcbDailyLogRepository.sumDieselLtrBefore(vehicleId, monthStart);
        BigDecimal tipperBefore = tipperDailyLogRepository.sumDieselLtrBefore(vehicleId, monthStart);
        return receivedBefore.subtract(jcbBefore).subtract(tipperBefore);
    }
}

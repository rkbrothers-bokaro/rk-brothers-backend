package com.boltblazers.rkbrothers.modules.fleet.dashboard;

import com.boltblazers.rkbrothers.core.masters.vehicle.Vehicle;
import com.boltblazers.rkbrothers.core.masters.vehicle.VehicleRepository;
import com.boltblazers.rkbrothers.core.masters.vehicle.VehicleStatus;
import com.boltblazers.rkbrothers.modules.fleet.dashboard.dto.DashboardSummaryDto;
import com.boltblazers.rkbrothers.modules.fleet.diesel.DieselStockService;
import com.boltblazers.rkbrothers.modules.fleet.diesel.FuelAnomalyService;
import com.boltblazers.rkbrothers.modules.fleet.diesel.dto.DieselStockRowDto;
import com.boltblazers.rkbrothers.modules.fleet.document.VehicleDocumentService;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Every value here defaults to zero/empty on missing data rather than
 * propagating nulls — this is a read-only aggregate view, so a quiet day
 * with no logs/receipts/documents should render a blank dashboard, not 500.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final int DOCUMENT_EXPIRY_WINDOW_DAYS = 30;

    private final VehicleRepository vehicleRepository;
    private final JcbDailyLogRepository jcbDailyLogRepository;
    private final TipperDailyLogRepository tipperDailyLogRepository;
    private final DieselStockService dieselStockService;
    private final FuelAnomalyService fuelAnomalyService;
    private final VehicleDocumentService vehicleDocumentService;

    public DashboardSummaryDto getSummary() {
        List<Vehicle> activeVehicles = vehicleRepository.findAllByIsActiveTrue(Pageable.unpaged()).getContent();
        long vehiclesTotal = activeVehicles.size();
        long vehiclesWorking = activeVehicles.stream()
                .filter(v -> v.getStatus() == VehicleStatus.WORKING)
                .count();

        BigDecimal hoursLoggedToday = computeHoursLoggedToday();
        BigDecimal dieselInStock = computeDieselInStock();
        int openAlertsCount = safeSize(fuelAnomalyService.detectAnomalies());
        int documentsExpiringCount = safeSize(vehicleDocumentService.getExpiringDocuments(DOCUMENT_EXPIRY_WINDOW_DAYS));

        return new DashboardSummaryDto(vehiclesWorking, vehiclesTotal, hoursLoggedToday, dieselInStock,
                openAlertsCount, documentsExpiringCount);
    }

    private BigDecimal computeHoursLoggedToday() {
        LocalDate today = LocalDate.now();

        BigDecimal jcbHours = jcbDailyLogRepository.findAllWithFilters(null, null, null, today, today, null, Pageable.unpaged())
                .stream()
                .map(JcbDailyLog::getTotalHrs)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tipperHours = tipperDailyLogRepository.findAllWithFilters(null, null, null, today, today, null, Pageable.unpaged())
                .stream()
                .map(TipperDailyLog::getTotalHrs)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return jcbHours.add(tipperHours);
    }

    private BigDecimal computeDieselInStock() {
        LocalDate today = LocalDate.now();
        List<DieselStockRowDto> rows = dieselStockService.getMonthlyRegister(today.getMonthValue(), today.getYear(), null);

        // Rows are sorted by date ascending; the last row per vehicle holds
        // that vehicle's most current closing balance this month.
        Map<Long, BigDecimal> latestClosingBalancePerVehicle = new LinkedHashMap<>();
        for (DieselStockRowDto row : rows) {
            latestClosingBalancePerVehicle.put(row.vehicleId(), row.closingBalance());
        }

        return latestClosingBalancePerVehicle.values().stream()
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int safeSize(List<?> list) {
        return list != null ? list.size() : 0;
    }
}

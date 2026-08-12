package com.boltblazers.rkbrothers.modules.fleet.diesel;

import com.boltblazers.rkbrothers.core.masters.vehicle.Vehicle;
import com.boltblazers.rkbrothers.core.masters.vehicle.VehicleRepository;
import com.boltblazers.rkbrothers.modules.fleet.diesel.dto.FuelAnomalyDto;
import com.boltblazers.rkbrothers.modules.fleet.jcb.JcbDailyLog;
import com.boltblazers.rkbrothers.modules.fleet.jcb.JcbDailyLogRepository;
import com.boltblazers.rkbrothers.modules.fleet.tipper.TipperDailyLog;
import com.boltblazers.rkbrothers.modules.fleet.tipper.TipperDailyLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Anomalies are recomputed fresh on every call — there is no persisted
 * store; "in-memory" here just means we never write this to the database.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FuelAnomalyService {

    private static final String ANOMALY_TYPE = "HIGH_CONSUMPTION";
    private static final BigDecimal SPIKE_THRESHOLD_MULTIPLIER = BigDecimal.valueOf(1.25);
    private static final Set<String> JCB_TYPES = Set.of("jcb", "poclain");
    private static final Set<String> TIPPER_TYPES = Set.of("tipper", "hyva");

    private final VehicleRepository vehicleRepository;
    private final JcbDailyLogRepository jcbDailyLogRepository;
    private final TipperDailyLogRepository tipperDailyLogRepository;

    public List<FuelAnomalyDto> detectAnomalies() {
        LocalDate today = LocalDate.now();
        List<Vehicle> vehicles = vehicleRepository.findAllByIsActiveTrue(Pageable.unpaged()).getContent();

        List<FuelAnomalyDto> anomalies = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            BigDecimal todayAvg = getAvgForDate(vehicle, today);
            if (todayAvg == null) {
                continue;
            }

            List<BigDecimal> rollingValues = getAvgsForRange(vehicle, today.minusDays(7), today.minusDays(1));
            if (rollingValues.isEmpty()) {
                continue;
            }

            BigDecimal rollingAvg = average(rollingValues);
            if (rollingAvg.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal threshold = rollingAvg.multiply(SPIKE_THRESHOLD_MULTIPLIER);
            if (todayAvg.compareTo(threshold) > 0) {
                BigDecimal spikePercentage = todayAvg.subtract(rollingAvg)
                        .divide(rollingAvg, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);

                anomalies.add(new FuelAnomalyDto(
                        vehicle.getId(),
                        vehicle.getVehicleNo(),
                        vehicle.getDisplayName(),
                        todayAvg,
                        rollingAvg.setScale(2, RoundingMode.HALF_UP),
                        spikePercentage,
                        today,
                        ANOMALY_TYPE
                ));
            }
        }

        return anomalies;
    }

    private BigDecimal getAvgForDate(Vehicle vehicle, LocalDate date) {
        String type = vehicle.getType() != null ? vehicle.getType().toLowerCase() : "";
        if (JCB_TYPES.contains(type)) {
            return jcbDailyLogRepository.findByVehicleIdAndDate(vehicle.getId(), date)
                    .map(JcbDailyLog::getAvgLtrPerHr)
                    .orElse(null);
        }
        if (TIPPER_TYPES.contains(type)) {
            return tipperDailyLogRepository.findByVehicleIdAndDate(vehicle.getId(), date)
                    .map(TipperDailyLog::getAvgKmPerLtr)
                    .orElse(null);
        }
        return null;
    }

    private List<BigDecimal> getAvgsForRange(Vehicle vehicle, LocalDate startDate, LocalDate endDate) {
        String type = vehicle.getType() != null ? vehicle.getType().toLowerCase() : "";
        if (JCB_TYPES.contains(type)) {
            return jcbDailyLogRepository.findByVehicleIdAndDateBetween(vehicle.getId(), startDate, endDate).stream()
                    .map(JcbDailyLog::getAvgLtrPerHr)
                    .filter(java.util.Objects::nonNull)
                    .toList();
        }
        if (TIPPER_TYPES.contains(type)) {
            return tipperDailyLogRepository.findByVehicleIdAndDateBetween(vehicle.getId(), startDate, endDate).stream()
                    .map(TipperDailyLog::getAvgKmPerLtr)
                    .filter(java.util.Objects::nonNull)
                    .toList();
        }
        return List.of();
    }

    private BigDecimal average(List<BigDecimal> values) {
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);
    }
}

package com.boltblazers.rkbrothers.modules.fleet.tipper;

import com.boltblazers.rkbrothers.core.audit.AuditAction;
import com.boltblazers.rkbrothers.core.audit.Auditable;
import com.boltblazers.rkbrothers.core.auth.Role;
import com.boltblazers.rkbrothers.core.auth.UserPrincipal;
import com.boltblazers.rkbrothers.core.auth.UserRepository;
import com.boltblazers.rkbrothers.core.common.BadRequestException;
import com.boltblazers.rkbrothers.core.common.ResourceNotFoundException;
import com.boltblazers.rkbrothers.core.masters.operator.Operator;
import com.boltblazers.rkbrothers.core.masters.operator.OperatorRepository;
import com.boltblazers.rkbrothers.core.masters.vehicle.Vehicle;
import com.boltblazers.rkbrothers.core.masters.vehicle.VehicleRepository;
import com.boltblazers.rkbrothers.core.masters.workorder.WorkOrder;
import com.boltblazers.rkbrothers.core.masters.workorder.WorkOrderRepository;
import com.boltblazers.rkbrothers.modules.fleet.tipper.dto.TipperDailyLogRequestDto;
import com.boltblazers.rkbrothers.modules.fleet.tipper.dto.TipperDailyLogResponseDto;
import com.boltblazers.rkbrothers.modules.fleet.tipper.dto.TipperHrEntryDto;
import com.boltblazers.rkbrothers.modules.fleet.tipper.dto.TipperTripEntryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class TipperDailyLogService {

    private static final String ENTITY_NAME = "TipperDailyLog";
    private static final Set<String> ALLOWED_VEHICLE_TYPES = Set.of("tipper", "hyva");

    private final TipperDailyLogRepository tipperDailyLogRepository;
    private final VehicleRepository vehicleRepository;
    private final WorkOrderRepository workOrderRepository;
    private final OperatorRepository operatorRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public BigDecimal getOpeningHrsForToday(Long vehicleId, LocalDate date) {
        return tipperDailyLogRepository.findByVehicleIdAndDate(vehicleId, date.minusDays(1))
                .map(TipperDailyLog::getClosingHrs)
                .orElse(null);
    }

    @Auditable(entityName = ENTITY_NAME, action = AuditAction.CREATE)
    public TipperDailyLogResponseDto createLog(TipperDailyLogRequestDto request, UserPrincipal currentUser) {
        Vehicle vehicle = resolveVehicle(request.vehicleId());
        WorkOrder workOrder = resolveWorkOrder(request.workOrderId());
        Operator driver = resolveDriver(request.driverId());

        BigDecimal totalHrs = computeDelta(request.openingHrs(), request.closingHrs());
        BigDecimal totalKm = computeDelta(request.openingKm(), request.closingKm());
        BigDecimal avgKmPerLtr = computeAvgKmPerLtr(totalKm, request.dieselLtr());

        TipperDailyLog log = TipperDailyLog.builder()
                .date(request.date())
                .vehicle(vehicle)
                .workOrder(workOrder)
                .driver(driver)
                .openingHrs(request.openingHrs())
                .closingHrs(request.closingHrs())
                .totalHrs(totalHrs)
                .openingKm(request.openingKm())
                .closingKm(request.closingKm())
                .totalKm(totalKm)
                .dieselLtr(request.dieselLtr())
                .dieselHrs(request.dieselHrs())
                .dieselKm(request.dieselKm())
                .runKm(request.runKm())
                .avgKmPerLtr(avgKmPerLtr)
                .workDescription(request.workDescription())
                .submittedBy(userRepository.getReferenceById(currentUser.getId()))
                .build();

        List<TipperTripEntry> tripEntries = buildTripEntries(request.tripEntries(), log);
        log.getTripEntries().addAll(tripEntries);
        log.getHrEntries().addAll(buildHrEntries(request.hrEntries(), log));
        log.setTripCount(resolveTripCount(request.tripCount(), tripEntries));

        return TipperDailyLogResponseDto.from(tipperDailyLogRepository.save(log));
    }

    @Transactional(readOnly = true)
    public Page<TipperDailyLogResponseDto> getLogs(Long vehicleId, Long workOrderId, Long driverId,
                                                    LocalDate startDate, LocalDate endDate,
                                                    int page, int size, UserPrincipal currentUser) {
        Long submittedById = currentUser.getRole() == Role.ADMIN ? null : currentUser.getId();
        Page<TipperDailyLog> logs = tipperDailyLogRepository.findAllWithFilters(
                vehicleId, workOrderId, driverId, startDate, endDate, submittedById, PageRequest.of(page, size));
        return logs.map(TipperDailyLogResponseDto::from);
    }

    @Transactional(readOnly = true)
    public TipperDailyLogResponseDto getLogById(Long id, UserPrincipal currentUser) {
        return TipperDailyLogResponseDto.from(findVisibleById(id, currentUser));
    }

    @Auditable(entityName = ENTITY_NAME, action = AuditAction.UPDATE)
    public TipperDailyLogResponseDto updateLog(Long id, TipperDailyLogRequestDto request, UserPrincipal currentUser) {
        requireAdmin(currentUser);

        TipperDailyLog log = tipperDailyLogRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(ENTITY_NAME, id));

        Vehicle vehicle = resolveVehicle(request.vehicleId());
        WorkOrder workOrder = resolveWorkOrder(request.workOrderId());
        Operator driver = resolveDriver(request.driverId());

        BigDecimal totalHrs = computeDelta(request.openingHrs(), request.closingHrs());
        BigDecimal totalKm = computeDelta(request.openingKm(), request.closingKm());
        BigDecimal avgKmPerLtr = computeAvgKmPerLtr(totalKm, request.dieselLtr());

        log.setDate(request.date());
        log.setVehicle(vehicle);
        log.setWorkOrder(workOrder);
        log.setDriver(driver);
        log.setOpeningHrs(request.openingHrs());
        log.setClosingHrs(request.closingHrs());
        log.setTotalHrs(totalHrs);
        log.setOpeningKm(request.openingKm());
        log.setClosingKm(request.closingKm());
        log.setTotalKm(totalKm);
        log.setDieselLtr(request.dieselLtr());
        log.setDieselHrs(request.dieselHrs());
        log.setDieselKm(request.dieselKm());
        log.setRunKm(request.runKm());
        log.setAvgKmPerLtr(avgKmPerLtr);
        log.setWorkDescription(request.workDescription());

        log.getTripEntries().clear();
        List<TipperTripEntry> tripEntries = buildTripEntries(request.tripEntries(), log);
        log.getTripEntries().addAll(tripEntries);

        log.getHrEntries().clear();
        log.getHrEntries().addAll(buildHrEntries(request.hrEntries(), log));

        log.setTripCount(resolveTripCount(request.tripCount(), tripEntries));

        return TipperDailyLogResponseDto.from(tipperDailyLogRepository.save(log));
    }

    @Auditable(entityName = ENTITY_NAME, action = AuditAction.DELETE)
    public void deleteLog(Long id, UserPrincipal currentUser) {
        requireAdmin(currentUser);
        TipperDailyLog log = tipperDailyLogRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(ENTITY_NAME, id));
        tipperDailyLogRepository.delete(log);
    }

    private TipperDailyLog findVisibleById(Long id, UserPrincipal currentUser) {
        TipperDailyLog log = tipperDailyLogRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(ENTITY_NAME, id));

        boolean isOwner = log.getSubmittedBy() != null && log.getSubmittedBy().getId().equals(currentUser.getId());
        if (currentUser.getRole() != Role.ADMIN && !isOwner) {
            throw ResourceNotFoundException.of(ENTITY_NAME, id);
        }
        return log;
    }

    private void requireAdmin(UserPrincipal currentUser) {
        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can perform this action");
        }
    }

    private Vehicle resolveVehicle(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .filter(Vehicle::isActive)
                .orElseThrow(() -> ResourceNotFoundException.of("Vehicle", vehicleId));

        if (vehicle.getType() == null || !ALLOWED_VEHICLE_TYPES.contains(vehicle.getType().toLowerCase())) {
            throw new BadRequestException("Vehicle type mismatch");
        }
        return vehicle;
    }

    private WorkOrder resolveWorkOrder(Long workOrderId) {
        if (workOrderId == null) {
            return null;
        }
        return workOrderRepository.findById(workOrderId)
                .filter(WorkOrder::isActive)
                .orElseThrow(() -> ResourceNotFoundException.of("WorkOrder", workOrderId));
    }

    private Operator resolveDriver(Long driverId) {
        if (driverId == null) {
            return null;
        }
        return operatorRepository.findById(driverId)
                .filter(Operator::isActive)
                .orElseThrow(() -> ResourceNotFoundException.of("Operator", driverId));
    }

    private BigDecimal computeDelta(BigDecimal opening, BigDecimal closing) {
        if (closing.compareTo(opening) < 0) {
            throw new BadRequestException("Validation failed");
        }
        return closing.subtract(opening);
    }

    private BigDecimal computeAvgKmPerLtr(BigDecimal totalKm, BigDecimal dieselLtr) {
        if (dieselLtr == null || dieselLtr.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return totalKm.divide(dieselLtr, 2, RoundingMode.HALF_UP);
    }

    private List<TipperTripEntry> buildTripEntries(List<TipperTripEntryDto> dtos, TipperDailyLog log) {
        List<TipperTripEntry> entries = new ArrayList<>();
        if (dtos == null) {
            return entries;
        }
        for (TipperTripEntryDto dto : dtos) {
            entries.add(TipperTripEntry.builder()
                    .log(log)
                    .fromLocation(dto.fromLocation())
                    .toLocation(dto.toLocation())
                    .trips(dto.trips() != null ? dto.trips() : 1)
                    .build());
        }
        return entries;
    }

    private List<TipperHrEntry> buildHrEntries(List<TipperHrEntryDto> dtos, TipperDailyLog log) {
        List<TipperHrEntry> entries = new ArrayList<>();
        if (dtos == null) {
            return entries;
        }
        for (TipperHrEntryDto dto : dtos) {
            BigDecimal entryTotalHrs = computeDelta(dto.openingHrs(), dto.closingHrs());
            entries.add(TipperHrEntry.builder()
                    .log(log)
                    .openingHrs(dto.openingHrs())
                    .closingHrs(dto.closingHrs())
                    .totalHrs(entryTotalHrs)
                    .build());
        }
        return entries;
    }

    private Integer resolveTripCount(Integer requestedTripCount, List<TipperTripEntry> tripEntries) {
        if (requestedTripCount != null) {
            return requestedTripCount;
        }
        return tripEntries.stream().mapToInt(TipperTripEntry::getTrips).sum();
    }
}

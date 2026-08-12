package com.boltblazers.rkbrothers.modules.fleet.jcb;

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
import com.boltblazers.rkbrothers.modules.fleet.jcb.dto.JcbDailyLogRequestDto;
import com.boltblazers.rkbrothers.modules.fleet.jcb.dto.JcbDailyLogResponseDto;
import com.boltblazers.rkbrothers.modules.fleet.jcb.dto.JcbShiftDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class JcbDailyLogService {

    private static final String ENTITY_NAME = "JcbDailyLog";
    private static final Set<String> ALLOWED_VEHICLE_TYPES = Set.of("jcb", "poclain");

    private final JcbDailyLogRepository jcbDailyLogRepository;
    private final VehicleRepository vehicleRepository;
    private final WorkOrderRepository workOrderRepository;
    private final OperatorRepository operatorRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public BigDecimal getOpeningHrsForToday(Long vehicleId, LocalDate date) {
        return jcbDailyLogRepository.findByVehicleIdAndDate(vehicleId, date.minusDays(1))
                .map(JcbDailyLog::getClosingHrs)
                .orElse(null);
    }

    @Auditable(entityName = ENTITY_NAME, action = AuditAction.CREATE)
    public JcbDailyLogResponseDto createLog(JcbDailyLogRequestDto request, UserPrincipal currentUser) {
        Vehicle vehicle = resolveVehicle(request.vehicleId());
        WorkOrder workOrder = resolveWorkOrder(request.workOrderId());
        Operator operator = resolveOperator(request.operatorId());

        BigDecimal totalHrs = computeTotalHrs(request.openingHrs(), request.closingHrs());
        BigDecimal avgLtrPerHr = computeAvgLtrPerHr(request.dieselLtr(), totalHrs);

        JcbDailyLog log = JcbDailyLog.builder()
                .date(request.date())
                .vehicle(vehicle)
                .workOrder(workOrder)
                .operator(operator)
                .openingHrs(request.openingHrs())
                .closingHrs(request.closingHrs())
                .totalHrs(totalHrs)
                .dieselLtr(request.dieselLtr())
                .dieselMtr(request.dieselMtr())
                .runningLtr(request.runningLtr())
                .avgLtrPerHr(avgLtrPerHr)
                .materialType(request.materialType())
                .materialQty(request.materialQty())
                .materialUnit(request.materialUnit())
                .workDescription(request.workDescription())
                .submittedBy(userRepository.getReferenceById(currentUser.getId()))
                .build();

        log.getShifts().addAll(buildShifts(request.shifts(), log));

        return JcbDailyLogResponseDto.from(jcbDailyLogRepository.save(log));
    }

    @Transactional(readOnly = true)
    public Page<JcbDailyLogResponseDto> getLogs(Long vehicleId, Long workOrderId, Long operatorId,
                                                 LocalDate startDate, LocalDate endDate,
                                                 int page, int size, UserPrincipal currentUser) {
        Long submittedById = currentUser.getRole() == Role.ADMIN ? null : currentUser.getId();
        Page<JcbDailyLog> logs = jcbDailyLogRepository.findAllWithFilters(
                vehicleId, workOrderId, operatorId, startDate, endDate, submittedById, PageRequest.of(page, size));
        return logs.map(JcbDailyLogResponseDto::from);
    }

    @Transactional(readOnly = true)
    public JcbDailyLogResponseDto getLogById(Long id, UserPrincipal currentUser) {
        return JcbDailyLogResponseDto.from(findVisibleById(id, currentUser));
    }

    @Auditable(entityName = ENTITY_NAME, action = AuditAction.UPDATE)
    public JcbDailyLogResponseDto updateLog(Long id, JcbDailyLogRequestDto request, UserPrincipal currentUser) {
        requireAdmin(currentUser);

        JcbDailyLog log = jcbDailyLogRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(ENTITY_NAME, id));

        Vehicle vehicle = resolveVehicle(request.vehicleId());
        WorkOrder workOrder = resolveWorkOrder(request.workOrderId());
        Operator operator = resolveOperator(request.operatorId());

        BigDecimal totalHrs = computeTotalHrs(request.openingHrs(), request.closingHrs());
        BigDecimal avgLtrPerHr = computeAvgLtrPerHr(request.dieselLtr(), totalHrs);

        log.setDate(request.date());
        log.setVehicle(vehicle);
        log.setWorkOrder(workOrder);
        log.setOperator(operator);
        log.setOpeningHrs(request.openingHrs());
        log.setClosingHrs(request.closingHrs());
        log.setTotalHrs(totalHrs);
        log.setDieselLtr(request.dieselLtr());
        log.setDieselMtr(request.dieselMtr());
        log.setRunningLtr(request.runningLtr());
        log.setAvgLtrPerHr(avgLtrPerHr);
        log.setMaterialType(request.materialType());
        log.setMaterialQty(request.materialQty());
        log.setMaterialUnit(request.materialUnit());
        log.setWorkDescription(request.workDescription());

        log.getShifts().clear();
        log.getShifts().addAll(buildShifts(request.shifts(), log));

        return JcbDailyLogResponseDto.from(jcbDailyLogRepository.save(log));
    }

    @Auditable(entityName = ENTITY_NAME, action = AuditAction.DELETE)
    public void deleteLog(Long id, UserPrincipal currentUser) {
        requireAdmin(currentUser);
        JcbDailyLog log = jcbDailyLogRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(ENTITY_NAME, id));
        jcbDailyLogRepository.delete(log);
    }

    private JcbDailyLog findVisibleById(Long id, UserPrincipal currentUser) {
        JcbDailyLog log = jcbDailyLogRepository.findById(id)
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

    private Operator resolveOperator(Long operatorId) {
        if (operatorId == null) {
            return null;
        }
        return operatorRepository.findById(operatorId)
                .filter(Operator::isActive)
                .orElseThrow(() -> ResourceNotFoundException.of("Operator", operatorId));
    }

    private BigDecimal computeTotalHrs(BigDecimal openingHrs, BigDecimal closingHrs) {
        if (closingHrs.compareTo(openingHrs) < 0) {
            throw new BadRequestException("Validation failed");
        }
        return closingHrs.subtract(openingHrs);
    }

    private BigDecimal computeAvgLtrPerHr(BigDecimal dieselLtr, BigDecimal totalHrs) {
        if (dieselLtr == null || totalHrs == null || totalHrs.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return dieselLtr.divide(totalHrs, 2, RoundingMode.HALF_UP);
    }

    private List<JcbDailyLogShift> buildShifts(List<JcbShiftDto> shiftDtos, JcbDailyLog log) {
        List<JcbDailyLogShift> shifts = new ArrayList<>();
        for (JcbShiftDto dto : shiftDtos) {
            if (!dto.closeTime().isAfter(dto.startTime())) {
                throw new BadRequestException("Validation failed");
            }
            BigDecimal totalTime = BigDecimal.valueOf(Duration.between(dto.startTime(), dto.closeTime()).toMinutes())
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            shifts.add(JcbDailyLogShift.builder()
                    .log(log)
                    .startTime(dto.startTime())
                    .closeTime(dto.closeTime())
                    .totalTime(totalTime)
                    .build());
        }
        return shifts;
    }
}

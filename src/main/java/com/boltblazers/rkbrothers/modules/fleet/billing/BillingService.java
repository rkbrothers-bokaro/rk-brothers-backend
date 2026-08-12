package com.boltblazers.rkbrothers.modules.fleet.billing;

import com.boltblazers.rkbrothers.core.common.BadRequestException;
import com.boltblazers.rkbrothers.core.common.ResourceNotFoundException;
import com.boltblazers.rkbrothers.core.masters.party.Party;
import com.boltblazers.rkbrothers.core.masters.vehicle.Vehicle;
import com.boltblazers.rkbrothers.core.masters.workorder.WorkOrder;
import com.boltblazers.rkbrothers.core.masters.workorder.WorkOrderRepository;
import com.boltblazers.rkbrothers.modules.fleet.billing.dto.BillEntryDto;
import com.boltblazers.rkbrothers.modules.fleet.billing.dto.BillLineItemDto;
import com.boltblazers.rkbrothers.modules.fleet.billing.dto.BillSummaryDto;
import com.boltblazers.rkbrothers.modules.fleet.billing.dto.BillablePeriodDto;
import com.boltblazers.rkbrothers.modules.fleet.jcb.JcbDailyLog;
import com.boltblazers.rkbrothers.modules.fleet.jcb.JcbDailyLogRepository;
import com.boltblazers.rkbrothers.modules.fleet.tipper.TipperDailyLog;
import com.boltblazers.rkbrothers.modules.fleet.tipper.TipperDailyLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BillingService {

    private static final String RAILLINE_UNIT = "Rail Line (13m)";

    private final WorkOrderRepository workOrderRepository;
    private final JcbDailyLogRepository jcbDailyLogRepository;
    private final TipperDailyLogRepository tipperDailyLogRepository;

    public BillSummaryDto generateBill(Long workOrderId, int month, int year) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .filter(WorkOrder::isActive)
                .orElseThrow(() -> ResourceNotFoundException.of("WorkOrder", workOrderId));

        Party party = workOrder.getParty();
        String billingBasis = workOrder.getBillingBasis() != null ? workOrder.getBillingBasis().toLowerCase() : "";
        BigDecimal rate = workOrder.getRate() != null ? workOrder.getRate() : BigDecimal.ZERO;

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<BillLineItemDto> lineItems = switch (billingBasis) {
            case "hour" -> buildHourLineItems(workOrder, party, rate, startDate, endDate);
            case "trip" -> buildTripLineItems(workOrder, party, rate, startDate, endDate, "trip", workOrder.getUnit());
            case "km" -> buildKmLineItems(workOrder, party, rate, startDate, endDate);
            case "railline" -> buildTripLineItems(workOrder, party, rate, startDate, endDate, "railline", RAILLINE_UNIT);
            default -> throw new BadRequestException("Unsupported billing basis: " + workOrder.getBillingBasis());
        };

        BigDecimal totalAmount = lineItems.stream()
                .map(BillLineItemDto::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new BillSummaryDto(
                workOrder.getId(),
                workOrder.getWoNumber(),
                party != null ? party.getId() : null,
                party != null ? party.getName() : null,
                workOrder.getSiteLocation(),
                month,
                year,
                lineItems,
                totalAmount,
                LocalDateTime.now()
        );
    }

    public List<BillablePeriodDto> getAllBillablePeriods() {
        record PeriodKey(Long workOrderId, int year, int month) {
        }

        Set<PeriodKey> combos = new LinkedHashSet<>();
        for (Object[] row : jcbDailyLogRepository.findWorkOrderDatesForBilling()) {
            LocalDate date = (LocalDate) row[1];
            combos.add(new PeriodKey((Long) row[0], date.getYear(), date.getMonthValue()));
        }
        for (Object[] row : tipperDailyLogRepository.findWorkOrderDatesForBilling()) {
            LocalDate date = (LocalDate) row[1];
            combos.add(new PeriodKey((Long) row[0], date.getYear(), date.getMonthValue()));
        }

        List<BillablePeriodDto> periods = new ArrayList<>();
        for (PeriodKey key : combos) {
            workOrderRepository.findById(key.workOrderId())
                    .ifPresent(wo -> periods.add(new BillablePeriodDto(wo.getId(), wo.getWoNumber(), key.month(), key.year())));
        }

        return periods.stream()
                .sorted(Comparator.comparing(BillablePeriodDto::year)
                        .thenComparing(BillablePeriodDto::month)
                        .thenComparing(BillablePeriodDto::woNumber))
                .toList();
    }

    private List<BillLineItemDto> buildHourLineItems(WorkOrder workOrder, Party party, BigDecimal rate,
                                                      LocalDate startDate, LocalDate endDate) {
        List<JcbDailyLog> logs = jcbDailyLogRepository.findByWorkOrderIdAndDateBetween(workOrder.getId(), startDate, endDate);

        Map<Long, List<JcbDailyLog>> byVehicle = logs.stream()
                .filter(l -> l.getVehicle() != null)
                .collect(Collectors.groupingBy(l -> l.getVehicle().getId()));

        List<BillLineItemDto> items = new ArrayList<>();
        for (List<JcbDailyLog> vehicleLogs : byVehicle.values()) {
            Vehicle vehicle = vehicleLogs.get(0).getVehicle();
            BigDecimal quantity = vehicleLogs.stream()
                    .map(JcbDailyLog::getTotalHrs)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<BillEntryDto> entries = vehicleLogs.stream()
                    .sorted(Comparator.comparing(JcbDailyLog::getDate))
                    .map(l -> new BillEntryDto(l.getDate(), l.getTotalHrs()))
                    .toList();

            items.add(buildLineItem(vehicle, workOrder, party, "hour", quantity, rate, workOrder.getUnit(), entries));
        }
        return items;
    }

    private List<BillLineItemDto> buildTripLineItems(WorkOrder workOrder, Party party, BigDecimal rate,
                                                      LocalDate startDate, LocalDate endDate,
                                                      String billingBasisLabel, String unit) {
        List<TipperDailyLog> logs = tipperDailyLogRepository.findByWorkOrderIdAndDateBetween(workOrder.getId(), startDate, endDate);

        Map<Long, List<TipperDailyLog>> byVehicle = logs.stream()
                .filter(l -> l.getVehicle() != null)
                .collect(Collectors.groupingBy(l -> l.getVehicle().getId()));

        List<BillLineItemDto> items = new ArrayList<>();
        for (List<TipperDailyLog> vehicleLogs : byVehicle.values()) {
            Vehicle vehicle = vehicleLogs.get(0).getVehicle();
            BigDecimal quantity = vehicleLogs.stream()
                    .map(l -> l.getTripCount() != null ? BigDecimal.valueOf(l.getTripCount()) : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<BillEntryDto> entries = vehicleLogs.stream()
                    .sorted(Comparator.comparing(TipperDailyLog::getDate))
                    .map(l -> new BillEntryDto(l.getDate(), l.getTripCount() != null ? BigDecimal.valueOf(l.getTripCount()) : BigDecimal.ZERO))
                    .toList();

            items.add(buildLineItem(vehicle, workOrder, party, billingBasisLabel, quantity, rate, unit, entries));
        }
        return items;
    }

    private List<BillLineItemDto> buildKmLineItems(WorkOrder workOrder, Party party, BigDecimal rate,
                                                    LocalDate startDate, LocalDate endDate) {
        List<TipperDailyLog> logs = tipperDailyLogRepository.findByWorkOrderIdAndDateBetween(workOrder.getId(), startDate, endDate);

        Map<Long, List<TipperDailyLog>> byVehicle = logs.stream()
                .filter(l -> l.getVehicle() != null)
                .collect(Collectors.groupingBy(l -> l.getVehicle().getId()));

        List<BillLineItemDto> items = new ArrayList<>();
        for (List<TipperDailyLog> vehicleLogs : byVehicle.values()) {
            Vehicle vehicle = vehicleLogs.get(0).getVehicle();
            BigDecimal quantity = vehicleLogs.stream()
                    .map(TipperDailyLog::getTotalKm)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<BillEntryDto> entries = vehicleLogs.stream()
                    .sorted(Comparator.comparing(TipperDailyLog::getDate))
                    .map(l -> new BillEntryDto(l.getDate(), l.getTotalKm()))
                    .toList();

            items.add(buildLineItem(vehicle, workOrder, party, "km", quantity, rate, workOrder.getUnit(), entries));
        }
        return items;
    }

    private BillLineItemDto buildLineItem(Vehicle vehicle, WorkOrder workOrder, Party party, String billingBasis,
                                           BigDecimal quantity, BigDecimal rate, String unit, List<BillEntryDto> entries) {
        return new BillLineItemDto(
                vehicle.getId(),
                vehicle.getVehicleNo(),
                vehicle.getDisplayName(),
                vehicle.getType(),
                workOrder.getId(),
                workOrder.getWoNumber(),
                party != null ? party.getName() : null,
                billingBasis,
                quantity,
                rate,
                quantity.multiply(rate),
                unit,
                entries
        );
    }
}

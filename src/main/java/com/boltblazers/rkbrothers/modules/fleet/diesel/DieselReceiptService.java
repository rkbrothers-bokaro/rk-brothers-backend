package com.boltblazers.rkbrothers.modules.fleet.diesel;

import com.boltblazers.rkbrothers.core.audit.AuditAction;
import com.boltblazers.rkbrothers.core.audit.Auditable;
import com.boltblazers.rkbrothers.core.auth.UserPrincipal;
import com.boltblazers.rkbrothers.core.auth.UserRepository;
import com.boltblazers.rkbrothers.core.common.ResourceNotFoundException;
import com.boltblazers.rkbrothers.core.masters.vehicle.Vehicle;
import com.boltblazers.rkbrothers.core.masters.vehicle.VehicleRepository;
import com.boltblazers.rkbrothers.modules.fleet.diesel.dto.DieselReceiptRequestDto;
import com.boltblazers.rkbrothers.modules.fleet.diesel.dto.DieselReceiptResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DieselReceiptService {

    private static final String ENTITY_NAME = "DieselReceipt";

    private final DieselReceiptRepository dieselReceiptRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    @Auditable(entityName = ENTITY_NAME, action = AuditAction.CREATE)
    public DieselReceiptResponseDto createReceipt(DieselReceiptRequestDto request, UserPrincipal currentUser) {
        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .filter(Vehicle::isActive)
                .orElseThrow(() -> ResourceNotFoundException.of("Vehicle", request.vehicleId()));

        DieselReceipt receipt = DieselReceipt.builder()
                .date(request.date())
                .vehicle(vehicle)
                .litres(request.litres())
                .receivedFrom(request.receivedFrom())
                .invoiceNo(request.invoiceNo())
                .enteredBy(userRepository.getReferenceById(currentUser.getId()))
                .createdAt(LocalDateTime.now())
                .build();

        return DieselReceiptResponseDto.from(dieselReceiptRepository.save(receipt));
    }

    @Transactional(readOnly = true)
    public List<DieselReceiptResponseDto> getReceipts(Long vehicleId, int month, int year) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<DieselReceipt> receipts = vehicleId != null
                ? dieselReceiptRepository.findByVehicleIdAndDateBetween(vehicleId, startDate, endDate)
                : dieselReceiptRepository.findByDateBetween(startDate, endDate);

        return receipts.stream().map(DieselReceiptResponseDto::from).toList();
    }

    @Auditable(entityName = ENTITY_NAME, action = AuditAction.DELETE)
    public void deleteReceipt(Long id) {
        DieselReceipt receipt = dieselReceiptRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(ENTITY_NAME, id));
        dieselReceiptRepository.delete(receipt);
    }
}

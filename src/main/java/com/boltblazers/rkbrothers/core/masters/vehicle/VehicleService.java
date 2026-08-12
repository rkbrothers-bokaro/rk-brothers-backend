package com.boltblazers.rkbrothers.core.masters.vehicle;

import com.boltblazers.rkbrothers.core.audit.AuditAction;
import com.boltblazers.rkbrothers.core.audit.Auditable;
import com.boltblazers.rkbrothers.core.common.DuplicateResourceException;
import com.boltblazers.rkbrothers.core.common.ResourceNotFoundException;
import com.boltblazers.rkbrothers.core.masters.operator.Operator;
import com.boltblazers.rkbrothers.core.masters.operator.OperatorRepository;
import com.boltblazers.rkbrothers.core.masters.vehicle.dto.VehicleRequest;
import com.boltblazers.rkbrothers.core.masters.vehicle.dto.VehicleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class VehicleService {

    private static final String ENTITY_NAME = "Vehicle";

    private final VehicleRepository vehicleRepository;
    private final OperatorRepository operatorRepository;

    @Transactional(readOnly = true)
    public Page<VehicleResponse> getAllVehicles(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Vehicle> vehicles = (search == null || search.isBlank())
                ? vehicleRepository.findAllByIsActiveTrue(pageable)
                : vehicleRepository.findByVehicleNoContainingIgnoreCaseAndIsActiveTrue(search, pageable);
        return vehicles.map(VehicleResponse::from);
    }

    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long id) {
        return VehicleResponse.from(findActiveById(id));
    }

    @Auditable(entityName = ENTITY_NAME, action = AuditAction.CREATE)
    public VehicleResponse createVehicle(VehicleRequest request) {
        if (vehicleRepository.existsByVehicleNo(request.vehicleNo())) {
            throw new DuplicateResourceException("Vehicle number already exists: " + request.vehicleNo());
        }

        Vehicle vehicle = Vehicle.builder()
                .vehicleNo(request.vehicleNo())
                .displayName(request.displayName())
                .type(request.type())
                .billingBasis(request.billingBasis())
                .assignedOperator(resolveOperator(request.assignedOperatorId()))
                .status(request.status())
                .build();
        return VehicleResponse.from(vehicleRepository.save(vehicle));
    }

    @Auditable(entityName = ENTITY_NAME, action = AuditAction.UPDATE)
    public VehicleResponse updateVehicle(Long id, VehicleRequest request) {
        Vehicle vehicle = findActiveById(id);

        if (!vehicle.getVehicleNo().equalsIgnoreCase(request.vehicleNo())
                && vehicleRepository.existsByVehicleNo(request.vehicleNo())) {
            throw new DuplicateResourceException("Vehicle number already exists: " + request.vehicleNo());
        }

        vehicle.setVehicleNo(request.vehicleNo());
        vehicle.setDisplayName(request.displayName());
        vehicle.setType(request.type());
        vehicle.setBillingBasis(request.billingBasis());
        vehicle.setAssignedOperator(resolveOperator(request.assignedOperatorId()));
        vehicle.setStatus(request.status());
        return VehicleResponse.from(vehicleRepository.save(vehicle));
    }

    @Auditable(entityName = ENTITY_NAME, action = AuditAction.DELETE)
    public void deleteVehicle(Long id) {
        Vehicle vehicle = findActiveById(id);
        vehicle.setActive(false);
        vehicleRepository.save(vehicle);
    }

    private Vehicle findActiveById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(ENTITY_NAME, id));
        if (!vehicle.isActive()) {
            throw ResourceNotFoundException.of(ENTITY_NAME, id);
        }
        return vehicle;
    }

    private Operator resolveOperator(Long operatorId) {
        if (operatorId == null) {
            return null;
        }
        return operatorRepository.findById(operatorId)
                .orElseThrow(() -> ResourceNotFoundException.of("Operator", operatorId));
    }
}

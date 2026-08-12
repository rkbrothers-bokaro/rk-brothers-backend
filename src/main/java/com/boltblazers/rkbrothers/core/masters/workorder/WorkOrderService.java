package com.boltblazers.rkbrothers.core.masters.workorder;

import com.boltblazers.rkbrothers.core.audit.AuditAction;
import com.boltblazers.rkbrothers.core.audit.Auditable;
import com.boltblazers.rkbrothers.core.common.DuplicateResourceException;
import com.boltblazers.rkbrothers.core.common.ResourceNotFoundException;
import com.boltblazers.rkbrothers.core.masters.party.Party;
import com.boltblazers.rkbrothers.core.masters.party.PartyRepository;
import com.boltblazers.rkbrothers.core.masters.workorder.dto.WorkOrderRequest;
import com.boltblazers.rkbrothers.core.masters.workorder.dto.WorkOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkOrderService {

    private static final String ENTITY_NAME = "WorkOrder";

    private final WorkOrderRepository workOrderRepository;
    private final PartyRepository partyRepository;

    @Transactional(readOnly = true)
    public Page<WorkOrderResponse> getAllWorkOrders(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<WorkOrder> workOrders = (search == null || search.isBlank())
                ? workOrderRepository.findAllByIsActiveTrue(pageable)
                : workOrderRepository.findByWoNumberContainingIgnoreCaseAndIsActiveTrue(search, pageable);
        return workOrders.map(WorkOrderResponse::from);
    }

    @Transactional(readOnly = true)
    public List<WorkOrderResponse> getWorkOrdersByParty(Long partyId) {
        return workOrderRepository.findAllByPartyId(partyId).stream()
                .filter(WorkOrder::isActive)
                .map(WorkOrderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkOrderResponse getWorkOrderById(Long id) {
        return WorkOrderResponse.from(findActiveById(id));
    }

    @Auditable(entityName = ENTITY_NAME, action = AuditAction.CREATE)
    public WorkOrderResponse createWorkOrder(WorkOrderRequest request) {
        if (workOrderRepository.existsByWoNumber(request.woNumber())) {
            throw new DuplicateResourceException("Work order number already exists: " + request.woNumber());
        }

        WorkOrder workOrder = WorkOrder.builder()
                .woNumber(request.woNumber())
                .party(resolveParty(request.partyId()))
                .description(request.description())
                .siteLocation(request.siteLocation())
                .billingBasis(request.billingBasis())
                .rate(request.rate())
                .unit(request.unit())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();
        return WorkOrderResponse.from(workOrderRepository.save(workOrder));
    }

    @Auditable(entityName = ENTITY_NAME, action = AuditAction.UPDATE)
    public WorkOrderResponse updateWorkOrder(Long id, WorkOrderRequest request) {
        WorkOrder workOrder = findActiveById(id);

        if (!workOrder.getWoNumber().equalsIgnoreCase(request.woNumber())
                && workOrderRepository.existsByWoNumber(request.woNumber())) {
            throw new DuplicateResourceException("Work order number already exists: " + request.woNumber());
        }

        workOrder.setWoNumber(request.woNumber());
        workOrder.setParty(resolveParty(request.partyId()));
        workOrder.setDescription(request.description());
        workOrder.setSiteLocation(request.siteLocation());
        workOrder.setBillingBasis(request.billingBasis());
        workOrder.setRate(request.rate());
        workOrder.setUnit(request.unit());
        workOrder.setStartDate(request.startDate());
        workOrder.setEndDate(request.endDate());
        return WorkOrderResponse.from(workOrderRepository.save(workOrder));
    }

    @Auditable(entityName = ENTITY_NAME, action = AuditAction.DELETE)
    public void deleteWorkOrder(Long id) {
        WorkOrder workOrder = findActiveById(id);
        workOrder.setActive(false);
        workOrderRepository.save(workOrder);
    }

    private WorkOrder findActiveById(Long id) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(ENTITY_NAME, id));
        if (!workOrder.isActive()) {
            throw ResourceNotFoundException.of(ENTITY_NAME, id);
        }
        return workOrder;
    }

    private Party resolveParty(Long partyId) {
        if (partyId == null) {
            return null;
        }
        return partyRepository.findById(partyId)
                .orElseThrow(() -> ResourceNotFoundException.of("Party", partyId));
    }
}

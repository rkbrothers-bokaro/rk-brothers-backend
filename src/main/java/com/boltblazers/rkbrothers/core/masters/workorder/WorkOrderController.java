package com.boltblazers.rkbrothers.core.masters.workorder;

import com.boltblazers.rkbrothers.core.common.ApiResponse;
import com.boltblazers.rkbrothers.core.masters.workorder.dto.WorkOrderRequest;
import com.boltblazers.rkbrothers.core.masters.workorder.dto.WorkOrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/masters/work-orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    @GetMapping
    public ApiResponse<Page<WorkOrderResponse>> findAll(@RequestParam(required = false) String search,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(workOrderService.getAllWorkOrders(search, page, size));
    }

    @GetMapping("/by-party/{partyId}")
    public ApiResponse<List<WorkOrderResponse>> findByParty(@PathVariable Long partyId) {
        return ApiResponse.success(workOrderService.getWorkOrdersByParty(partyId));
    }

    @GetMapping("/{id}")
    public ApiResponse<WorkOrderResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(workOrderService.getWorkOrderById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WorkOrderResponse>> create(@Valid @RequestBody WorkOrderRequest request) {
        WorkOrderResponse response = workOrderService.createWorkOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ApiResponse<WorkOrderResponse> update(@PathVariable Long id, @Valid @RequestBody WorkOrderRequest request) {
        return ApiResponse.success(workOrderService.updateWorkOrder(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        workOrderService.deleteWorkOrder(id);
        return ApiResponse.success("Work order deleted", null);
    }
}

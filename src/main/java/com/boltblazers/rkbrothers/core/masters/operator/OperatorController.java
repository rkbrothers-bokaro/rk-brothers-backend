package com.boltblazers.rkbrothers.core.masters.operator;

import com.boltblazers.rkbrothers.core.common.ApiResponse;
import com.boltblazers.rkbrothers.core.masters.operator.dto.OperatorRequest;
import com.boltblazers.rkbrothers.core.masters.operator.dto.OperatorResponse;
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

@RestController
@RequestMapping("/api/v1/masters/operators")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class OperatorController {

    private final OperatorService operatorService;

    @GetMapping
    public ApiResponse<Page<OperatorResponse>> findAll(@RequestParam(required = false) String search,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(operatorService.getAllOperators(search, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<OperatorResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(operatorService.getOperatorById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OperatorResponse>> create(@Valid @RequestBody OperatorRequest request) {
        OperatorResponse response = operatorService.createOperator(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ApiResponse<OperatorResponse> update(@PathVariable Long id, @Valid @RequestBody OperatorRequest request) {
        return ApiResponse.success(operatorService.updateOperator(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        operatorService.deleteOperator(id);
        return ApiResponse.success("Operator deleted", null);
    }
}

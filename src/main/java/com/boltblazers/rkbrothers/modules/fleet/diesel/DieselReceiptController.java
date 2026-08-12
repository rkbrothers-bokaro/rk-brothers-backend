package com.boltblazers.rkbrothers.modules.fleet.diesel;

import com.boltblazers.rkbrothers.core.auth.UserPrincipal;
import com.boltblazers.rkbrothers.core.common.ApiResponse;
import com.boltblazers.rkbrothers.modules.fleet.diesel.dto.DieselReceiptRequestDto;
import com.boltblazers.rkbrothers.modules.fleet.diesel.dto.DieselReceiptResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fleet/diesel/receipts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DieselReceiptController {

    private final DieselReceiptService dieselReceiptService;

    @PostMapping
    public ResponseEntity<ApiResponse<DieselReceiptResponseDto>> create(@Valid @RequestBody DieselReceiptRequestDto request,
                                                                         @AuthenticationPrincipal UserPrincipal currentUser) {
        DieselReceiptResponseDto response = dieselReceiptService.createReceipt(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ApiResponse<List<DieselReceiptResponseDto>> findAll(@RequestParam(required = false) Long vehicleId,
                                                                 @RequestParam int month,
                                                                 @RequestParam int year) {
        return ApiResponse.success(dieselReceiptService.getReceipts(vehicleId, month, year));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dieselReceiptService.deleteReceipt(id);
        return ApiResponse.success("Diesel receipt deleted", null);
    }
}

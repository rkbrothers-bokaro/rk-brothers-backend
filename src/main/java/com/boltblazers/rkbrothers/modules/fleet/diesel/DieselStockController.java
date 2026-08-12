package com.boltblazers.rkbrothers.modules.fleet.diesel;

import com.boltblazers.rkbrothers.core.common.ApiResponse;
import com.boltblazers.rkbrothers.modules.fleet.diesel.dto.DieselStockRowDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fleet/diesel/register")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DieselStockController {

    private final DieselStockService dieselStockService;

    @GetMapping
    public ApiResponse<List<DieselStockRowDto>> getRegister(@RequestParam int month,
                                                             @RequestParam int year,
                                                             @RequestParam(required = false) Long vehicleId) {
        return ApiResponse.success(dieselStockService.getMonthlyRegister(month, year, vehicleId));
    }
}

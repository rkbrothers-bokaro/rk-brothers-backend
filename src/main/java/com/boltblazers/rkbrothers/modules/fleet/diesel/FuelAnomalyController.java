package com.boltblazers.rkbrothers.modules.fleet.diesel;

import com.boltblazers.rkbrothers.core.common.ApiResponse;
import com.boltblazers.rkbrothers.modules.fleet.diesel.dto.FuelAnomalyDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fleet/diesel/anomalies")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class FuelAnomalyController {

    private final FuelAnomalyService fuelAnomalyService;

    @GetMapping
    public ApiResponse<List<FuelAnomalyDto>> getAnomalies() {
        return ApiResponse.success(fuelAnomalyService.detectAnomalies());
    }
}

package com.boltblazers.rkbrothers.modules.fleet.jcb;

import com.boltblazers.rkbrothers.core.auth.UserPrincipal;
import com.boltblazers.rkbrothers.core.common.ApiResponse;
import com.boltblazers.rkbrothers.modules.fleet.jcb.dto.JcbDailyLogRequestDto;
import com.boltblazers.rkbrothers.modules.fleet.jcb.dto.JcbDailyLogResponseDto;
import com.boltblazers.rkbrothers.modules.fleet.jcb.dto.OpeningHrsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/fleet/jcb-log")
@RequiredArgsConstructor
public class JcbDailyLogController {

    private final JcbDailyLogService jcbDailyLogService;

    @GetMapping("/opening-hrs")
    public ApiResponse<OpeningHrsResponse> openingHrs(@RequestParam Long vehicleId,
                                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.success(new OpeningHrsResponse(jcbDailyLogService.getOpeningHrsForToday(vehicleId, date)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JcbDailyLogResponseDto>> create(@Valid @RequestBody JcbDailyLogRequestDto request,
                                                                       @AuthenticationPrincipal UserPrincipal currentUser) {
        JcbDailyLogResponseDto response = jcbDailyLogService.createLog(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ApiResponse<Page<JcbDailyLogResponseDto>> findAll(@RequestParam(required = false) Long vehicleId,
                                                              @RequestParam(required = false) Long workOrderId,
                                                              @RequestParam(required = false) Long operatorId,
                                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size,
                                                              @AuthenticationPrincipal UserPrincipal currentUser) {
        return ApiResponse.success(jcbDailyLogService.getLogs(vehicleId, workOrderId, operatorId, startDate, endDate, page, size, currentUser));
    }

    @GetMapping("/{id}")
    public ApiResponse<JcbDailyLogResponseDto> findById(@PathVariable Long id,
                                                         @AuthenticationPrincipal UserPrincipal currentUser) {
        return ApiResponse.success(jcbDailyLogService.getLogById(id, currentUser));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<JcbDailyLogResponseDto> update(@PathVariable Long id,
                                                       @Valid @RequestBody JcbDailyLogRequestDto request,
                                                       @AuthenticationPrincipal UserPrincipal currentUser) {
        return ApiResponse.success(jcbDailyLogService.updateLog(id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal currentUser) {
        jcbDailyLogService.deleteLog(id, currentUser);
        return ApiResponse.success("Log deleted", null);
    }
}

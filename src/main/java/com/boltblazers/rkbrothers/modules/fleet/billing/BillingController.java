package com.boltblazers.rkbrothers.modules.fleet.billing;

import com.boltblazers.rkbrothers.core.common.ApiResponse;
import com.boltblazers.rkbrothers.modules.fleet.billing.dto.BillSummaryDto;
import com.boltblazers.rkbrothers.modules.fleet.billing.dto.BillablePeriodDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/fleet/billing")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BillingController {

    private final BillingService billingService;
    private final BillPdfService billPdfService;

    @GetMapping
    public ApiResponse<BillSummaryDto> getBill(@RequestParam Long workOrderId,
                                                @RequestParam int month,
                                                @RequestParam int year) {
        return ApiResponse.success(billingService.generateBill(workOrderId, month, year));
    }

    @GetMapping("/periods")
    public ApiResponse<List<BillablePeriodDto>> getBillablePeriods() {
        return ApiResponse.success(billingService.getAllBillablePeriods());
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> getBillPdf(@RequestParam Long workOrderId,
                                              @RequestParam int month,
                                              @RequestParam int year) {
        BillSummaryDto bill = billingService.generateBill(workOrderId, month, year);
        byte[] pdf = billPdfService.generatePdf(bill);

        String monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String safeWoNumber = bill.woNumber() != null ? bill.woNumber().replace("/", "") : "WO";
        String filename = "Bill_" + safeWoNumber + "_" + monthName + year + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .body(pdf);
    }
}

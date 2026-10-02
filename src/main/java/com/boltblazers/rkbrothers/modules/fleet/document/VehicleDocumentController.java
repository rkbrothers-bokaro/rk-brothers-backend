package com.boltblazers.rkbrothers.modules.fleet.document;

import com.boltblazers.rkbrothers.core.common.ApiResponse;
import com.boltblazers.rkbrothers.modules.fleet.document.dto.VehicleDocumentRequestDto;
import com.boltblazers.rkbrothers.modules.fleet.document.dto.VehicleDocumentResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import java.net.URI;


import java.util.List;

@RestController
@RequestMapping("/api/v1/fleet/documents")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class VehicleDocumentController {

    private final VehicleDocumentService vehicleDocumentService;

    @Value("${R2_PUBLIC_URL:}")
    private String publicUrl;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<VehicleDocumentResponseDto>> upload(@RequestParam Long vehicleId,
                                                                          @RequestParam String documentType,
                                                                          @RequestParam("file") MultipartFile file) {
        VehicleDocumentResponseDto response = vehicleDocumentService.uploadDocument(vehicleId, documentType, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{id}/confirm")
    public ApiResponse<VehicleDocumentResponseDto> confirm(@PathVariable Long id,
                                                            @Valid @RequestBody VehicleDocumentRequestDto request) {
        return ApiResponse.success(vehicleDocumentService.confirmDocument(id, request));
    }

    @GetMapping
    public ApiResponse<List<VehicleDocumentResponseDto>> byVehicle(@RequestParam(required = false) Long vehicleId) {
        if (vehicleId != null) {
            return ApiResponse.success(vehicleDocumentService.getDocumentsByVehicle(vehicleId));
        } else {
            return ApiResponse.success(vehicleDocumentService.getAllDocuments());
        }
    }

    @GetMapping("/expiring")
    public ApiResponse<List<VehicleDocumentResponseDto>> expiring(@RequestParam(defaultValue = "30") int days) {
        return ApiResponse.success(vehicleDocumentService.getExpiringDocuments(days));
    }

    @GetMapping("/{id}/file")
    public ApiResponse<String> viewFile(@PathVariable Long id) {
        String storageKey = vehicleDocumentService.getDocumentStorageKey(id);
        String redirectUrl = (publicUrl != null && !publicUrl.isBlank() ? publicUrl : "") + "/" + storageKey;
        return ApiResponse.success(redirectUrl);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        vehicleDocumentService.deleteDocument(id);
        return ApiResponse.success("Document deleted", null);
    }
}

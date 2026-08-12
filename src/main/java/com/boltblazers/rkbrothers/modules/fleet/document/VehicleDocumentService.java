package com.boltblazers.rkbrothers.modules.fleet.document;

import com.boltblazers.rkbrothers.core.audit.AuditAction;
import com.boltblazers.rkbrothers.core.audit.Auditable;
import com.boltblazers.rkbrothers.core.common.ResourceNotFoundException;
import com.boltblazers.rkbrothers.core.masters.vehicle.Vehicle;
import com.boltblazers.rkbrothers.core.masters.vehicle.VehicleRepository;
import com.boltblazers.rkbrothers.core.upload.FileUploadService;
import com.boltblazers.rkbrothers.core.upload.UploadedFile;
import com.boltblazers.rkbrothers.modules.fleet.document.dto.AiParseResultDto;
import com.boltblazers.rkbrothers.modules.fleet.document.dto.VehicleDocumentRequestDto;
import com.boltblazers.rkbrothers.modules.fleet.document.dto.VehicleDocumentResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VehicleDocumentService {

    private static final String ENTITY_NAME = "VehicleDocument";
    private static final String DOCUMENT_UPLOAD_FOLDER = "fleet/docs";
    private static final String HIGH_CONFIDENCE = "high";

    private final VehicleDocumentRepository vehicleDocumentRepository;
    private final VehicleRepository vehicleRepository;
    private final FileUploadService fileUploadService;
    private final GeminiVisionService geminiVisionService;
    private final ObjectMapper objectMapper;

    @Auditable(entityName = ENTITY_NAME, action = AuditAction.CREATE)
    public VehicleDocumentResponseDto uploadDocument(Long vehicleId, String documentType, MultipartFile file) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .filter(Vehicle::isActive)
                .orElseThrow(() -> ResourceNotFoundException.of("Vehicle", vehicleId));

        UploadedFile uploaded = fileUploadService.store(file, DOCUMENT_UPLOAD_FOLDER);
        AiParseResultDto aiResult = geminiVisionService.parseDocument(file);

        String resolvedDocumentType = (HIGH_CONFIDENCE.equalsIgnoreCase(aiResult.confidence()) && aiResult.documentType() != null)
                ? aiResult.documentType()
                : documentType;

        VehicleDocument document = VehicleDocument.builder()
                .vehicle(vehicle)
                .documentType(resolvedDocumentType)
                .documentNo(aiResult.documentNo())
                .issuedDate(aiResult.issuedDate())
                // expiry_date is NOT NULL in the schema, but AI extraction can
                // legitimately fail (e.g. no GEMINI_API_KEY, or an unreadable
                // scan). Default to today as an obvious "needs review" value
                // rather than fail the upload — confirmDocument() is exactly
                // the step where the admin is expected to set the real date.
                .expiryDate(aiResult.expiryDate() != null ? aiResult.expiryDate() : LocalDate.now())
                .documentUrl(uploaded.storageKey())
                .aiParsedData(toJson(aiResult))
                .build();

        return VehicleDocumentResponseDto.from(vehicleDocumentRepository.save(document));
    }

    @Auditable(entityName = ENTITY_NAME, action = AuditAction.UPDATE)
    public VehicleDocumentResponseDto confirmDocument(Long id, VehicleDocumentRequestDto request) {
        VehicleDocument document = vehicleDocumentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(ENTITY_NAME, id));

        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .filter(Vehicle::isActive)
                .orElseThrow(() -> ResourceNotFoundException.of("Vehicle", request.vehicleId()));

        document.setVehicle(vehicle);
        document.setDocumentType(request.documentType());
        document.setDocumentNo(request.documentNo());
        document.setIssuedDate(request.issuedDate());
        document.setExpiryDate(request.expiryDate());

        return VehicleDocumentResponseDto.from(vehicleDocumentRepository.save(document));
    }

    @Transactional(readOnly = true)
    public List<VehicleDocumentResponseDto> getDocumentsByVehicle(Long vehicleId) {
        return vehicleDocumentRepository.findByVehicleId(vehicleId).stream()
                .sorted(Comparator.comparing(VehicleDocument::getExpiryDate))
                .map(VehicleDocumentResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VehicleDocumentResponseDto> getExpiringDocuments(int days) {
        LocalDate today = LocalDate.now();
        return vehicleDocumentRepository.findByExpiryDateBetween(today, today.plusDays(days)).stream()
                .sorted(Comparator.comparing(VehicleDocument::getExpiryDate))
                .map(VehicleDocumentResponseDto::from)
                .toList();
    }

    @Auditable(entityName = ENTITY_NAME, action = AuditAction.DELETE)
    public void deleteDocument(Long id) {
        VehicleDocument document = vehicleDocumentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(ENTITY_NAME, id));
        vehicleDocumentRepository.delete(document);
    }

    private String toJson(AiParseResultDto aiResult) {
        try {
            return objectMapper.writeValueAsString(aiResult);
        } catch (Exception e) {
            log.warn("Failed to serialize AI parse result: {}", e.getMessage());
            return null;
        }
    }
}

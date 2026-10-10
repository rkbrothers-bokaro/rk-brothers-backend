package com.boltblazers.rkbrothers.core.upload;

import com.boltblazers.rkbrothers.core.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @Value("${R2_PUBLIC_URL:}")
    private String publicUrl;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "general") String folder) {
        
        UploadedFile uploaded = fileUploadService.store(file, folder);
        
        String url = (publicUrl != null && !publicUrl.isBlank() ? publicUrl : "") + "/" + uploaded.storageKey();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(Map.of(
                "url", url,
                "storageKey", uploaded.storageKey()
        )));
    }
}

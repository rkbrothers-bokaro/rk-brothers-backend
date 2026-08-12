package com.boltblazers.rkbrothers.core.masters.party;

import com.boltblazers.rkbrothers.core.common.ApiResponse;
import com.boltblazers.rkbrothers.core.masters.party.dto.PartyRequest;
import com.boltblazers.rkbrothers.core.masters.party.dto.PartyResponse;
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
@RequestMapping("/api/v1/masters/parties")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PartyController {

    private final PartyService partyService;

    @GetMapping
    public ApiResponse<Page<PartyResponse>> findAll(@RequestParam(required = false) String search,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(partyService.getAllParties(search, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<PartyResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(partyService.getPartyById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PartyResponse>> create(@Valid @RequestBody PartyRequest request) {
        PartyResponse response = partyService.createParty(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ApiResponse<PartyResponse> update(@PathVariable Long id, @Valid @RequestBody PartyRequest request) {
        return ApiResponse.success(partyService.updateParty(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        partyService.deleteParty(id);
        return ApiResponse.success("Party deleted", null);
    }
}

package com.boltblazers.rkbrothers.core.auth;

import com.boltblazers.rkbrothers.core.auth.dto.ResetPasswordRequestDto;
import com.boltblazers.rkbrothers.core.auth.dto.UserCreateRequestDto;
import com.boltblazers.rkbrothers.core.auth.dto.UserResponseDto;
import com.boltblazers.rkbrothers.core.auth.dto.UserUpdateRequestDto;
import com.boltblazers.rkbrothers.core.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserManagementService userManagementService;

    @GetMapping
    public ApiResponse<Page<UserResponseDto>> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(userManagementService.getAllUsers(page, size));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(@Valid @RequestBody UserCreateRequestDto request) {
        UserResponseDto response = userManagementService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponseDto> updateUser(@PathVariable Long id,
                                                    @Valid @RequestBody UserUpdateRequestDto request) {
        return ApiResponse.success(userManagementService.updateUser(id, request));
    }

    @PutMapping("/{id}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id,
                                            @Valid @RequestBody ResetPasswordRequestDto request) {
        userManagementService.resetPassword(id, request);
        return ApiResponse.success("Password reset", null);
    }
}

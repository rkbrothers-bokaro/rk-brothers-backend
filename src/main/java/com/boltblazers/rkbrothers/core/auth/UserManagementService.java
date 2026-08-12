package com.boltblazers.rkbrothers.core.auth;

import com.boltblazers.rkbrothers.core.audit.AuditAction;
import com.boltblazers.rkbrothers.core.audit.Auditable;
import com.boltblazers.rkbrothers.core.auth.dto.ResetPasswordRequestDto;
import com.boltblazers.rkbrothers.core.auth.dto.UserCreateRequestDto;
import com.boltblazers.rkbrothers.core.auth.dto.UserResponseDto;
import com.boltblazers.rkbrothers.core.auth.dto.UserUpdateRequestDto;
import com.boltblazers.rkbrothers.core.common.DuplicateResourceException;
import com.boltblazers.rkbrothers.core.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserManagementService {

    private static final String ENTITY_NAME = "User";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsers(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size)).map(UserResponseDto::from);
    }

    @Auditable(entityName = ENTITY_NAME, action = AuditAction.CREATE)
    public UserResponseDto createUser(UserCreateRequestDto request) {
        if (userRepository.existsByPhone(request.phone())) {
            throw new DuplicateResourceException("Phone already exists: " + request.phone());
        }

        // username/email are NOT NULL UNIQUE in the users table but are no
        // longer part of the request contract — derive them deterministically
        // from phone (already checked unique above) so the row can be saved
        // without ever surfacing these as user-facing fields.
        User user = User.builder()
                .username(request.phone())
                .phone(request.phone())
                .password(passwordEncoder.encode(request.password()))
                .email(request.phone() + "@users.rkbrothers.local")
                .fullName(request.name())
                .role(Role.valueOf(request.role().toUpperCase()))
                .enabled(true)
                .build();

        return UserResponseDto.from(userRepository.save(user));
    }

    @Auditable(entityName = ENTITY_NAME, action = AuditAction.UPDATE)
    public UserResponseDto updateUser(Long id, UserUpdateRequestDto request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(ENTITY_NAME, id));

        if (request.phone() != null && !request.phone().equalsIgnoreCase(user.getPhone())
                && userRepository.existsByPhone(request.phone())) {
            throw new DuplicateResourceException("Phone already exists: " + request.phone());
        }

        user.setFullName(request.name());
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        user.setRole(request.role());
        user.setEnabled(request.enabled());

        return UserResponseDto.from(userRepository.save(user));
    }

    @Auditable(entityName = ENTITY_NAME, action = AuditAction.UPDATE)
    public void resetPassword(Long id, ResetPasswordRequestDto request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(ENTITY_NAME, id));

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
}

package com.boltblazers.rkbrothers.core.auth.dto;

import com.boltblazers.rkbrothers.core.auth.UserPrincipal;

public record UserSummary(
        Long id,
        String name,
        String role
) {
    public static UserSummary from(UserPrincipal principal) {
        return new UserSummary(principal.getId(), principal.getName(), principal.getRole().name().toLowerCase());
    }
}

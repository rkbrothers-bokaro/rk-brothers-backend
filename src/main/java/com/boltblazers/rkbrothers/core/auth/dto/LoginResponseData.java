package com.boltblazers.rkbrothers.core.auth.dto;

public record LoginResponseData(
        String accessToken,
        String refreshToken,
        UserSummary user
) {
}

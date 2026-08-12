package com.boltblazers.rkbrothers.core.auth;

import com.boltblazers.rkbrothers.core.auth.dto.LoginRequest;
import com.boltblazers.rkbrothers.core.auth.dto.LoginResponseData;
import com.boltblazers.rkbrothers.core.auth.dto.RefreshRequest;
import com.boltblazers.rkbrothers.core.auth.dto.RefreshResponseData;
import com.boltblazers.rkbrothers.core.auth.dto.UserSummary;
import com.boltblazers.rkbrothers.core.common.ApiResponse;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseData>> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.phone(), request.password()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String accessToken = jwtUtil.generateAccessToken(principal);
        String refreshToken = jwtUtil.generateRefreshToken(principal);

        LoginResponseData data = new LoginResponseData(accessToken, refreshToken, UserSummary.from(principal));
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshResponseData>> refresh(@Valid @RequestBody RefreshRequest request) {
        String token = request.refreshToken();

        try {
            if (!jwtUtil.isRefreshToken(token)) {
                throw new BadCredentialsException("Not a refresh token");
            }

            String phone = jwtUtil.extractUsername(token);
            UserPrincipal principal = (UserPrincipal) userDetailsService.loadUserByUsername(phone);

            if (!jwtUtil.isTokenValid(token, principal)) {
                throw new BadCredentialsException("Invalid or expired refresh token");
            }

            String accessToken = jwtUtil.generateAccessToken(principal);
            return ResponseEntity.ok(ApiResponse.success(new RefreshResponseData(accessToken)));
        } catch (JwtException | IllegalArgumentException e) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success("Logged out", null));
    }
}

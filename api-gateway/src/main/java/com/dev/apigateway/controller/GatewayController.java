package com.dev.apigateway.controller;

import com.dev.apigateway.dto.AuthResponse;
import com.dev.apigateway.dto.LoginRequest;
import com.dev.apigateway.dto.RefreshTokenRequest;
import com.dev.apigateway.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/gateway/auth")
@RequiredArgsConstructor
@Slf4j
public class GatewayController {

    private final AuthService authService;

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> loginHandler(
            @Valid @RequestBody LoginRequest request
    ) {
        return authService.login(request)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().build()));
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<AuthResponse>> refreshTokenHandler(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        log.info("Refresh token: {}", request.refreshToken());
        return authService.refreshToken(request.refreshToken())
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().build()));
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logoutHandler(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return authService.logout(request.refreshToken())
                .map(result -> ResponseEntity.ok().<Void>build())
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().build()));
    }

    @GetMapping("/validate")
    public Mono<ResponseEntity<Boolean>> validateToken(Authentication authentication) {
        return Mono.just(ResponseEntity.ok(authentication != null && authentication.isAuthenticated()));
    }

}

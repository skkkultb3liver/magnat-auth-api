package com.dev.apigateway.service;

import com.dev.apigateway.dto.AuthResponse;
import com.dev.apigateway.dto.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;


@Service
public class AuthService {

    @Value("${app.config.keycloak.token-endpoint}")
    private String tokenEndpoint;

    @Value("${app.config.keycloak.logout-endpoint}")
    private String logoutEndpoint;

    @Value("${app.config.keycloak.client-id}")
    private String clientId;

    @Value("${app.config.keycloak.client-secret}")
    private String clientSecret;

    private final WebClient webClient;

    @Autowired
    public AuthService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<AuthResponse> login(LoginRequest request) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("username", request.username());
        form.add("password", request.password());

        return webClient.post()
                .uri(tokenEndpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(Map.class)
                .map(this::mapToAuthResponse)
                .onErrorMap(throwable -> new RuntimeException("Login failed: " + throwable.getMessage()));
    }

    public Mono<AuthResponse> refreshToken(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", refreshToken);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        return webClient.post()
                .uri(tokenEndpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(Map.class)
                .map(this::mapToAuthResponse)
                .onErrorMap(throwable -> new RuntimeException("Token refresh failed: " + throwable.getMessage()));
    }

    public Mono<Boolean> logout(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("refresh_token", refreshToken);

        return webClient.post()
                .uri(logoutEndpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> true)
                .onErrorReturn(false);
    }

    private AuthResponse mapToAuthResponse(Map<String, Object> response) {
        return new AuthResponse(
                (String) response.get("access_token"),
                (String) response.get("refresh_token"),
                (Integer) response.get("expires_in"),
                (Integer) response.get("refresh_expires_in")
        );
    }
}

package com.dev.apigateway;

import com.dev.apigateway.dto.AuthResponse;
import com.dev.apigateway.dto.LoginRequest;
import com.dev.apigateway.service.AuthService;
import com.netflix.discovery.converters.Auto;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureWebTestClient
public class GatewayControllerTest {

    @Autowired
    private WebTestClient client;

    @MockitoBean
    private AuthService authService;

    @Test
    void loginHandler_shouldReturnAuthResponse() {
        LoginRequest loginRequest = new LoginRequest("user", "password");

        AuthResponse mockResponse = new AuthResponse(
                "access_token",
                "refresh_token",
                3600,
                7200
        );

        when(authService.login(eq(loginRequest))).thenReturn(Mono.just(mockResponse));

        client.post()
                .uri("/api/v1/gateway/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .value(resp -> assertEquals("access_token", resp.accessToken()));
    }
}

package com.dev.apigateway;

import com.dev.apigateway.jwt.JwtMockConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureWebTestClient
@Import(JwtMockConfig.class)
public class TestControllerSecurityTest {

    @Autowired
    private WebTestClient webClient;

    @Test
    void adminAccess_withAdminRole_shouldBeOk() {
        webClient.mutateWith(
                        mockJwt().authorities(AuthorityUtils.createAuthorityList("ROLE_ADMIN"))
                )
                .get()
                .uri("/api/v1/admin/check")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void adminAccess_withoutAdminRole_shouldBeForbidden() {
        webClient.mutateWith(mockJwt().jwt(jwt -> jwt
                        .claim("realm_access", Map.of("roles", java.util.List.of("user")))))
                .get()
                .uri("/api/v1/admin/check")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void userAccess_shouldBeOk_withUserRole() {
        webClient.mutateWith(mockJwt().jwt(jwt -> jwt
                        .claim("realm_access", Map.of("roles", java.util.List.of("user")))))
                .get()
                .uri("/api/v1/user")
                .exchange()
                .expectStatus().isOk();
    }
}

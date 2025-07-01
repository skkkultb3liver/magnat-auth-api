package com.dev.apigateway.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
public class TestController {

    @GetMapping("/user")
    public Mono<Void> userEndpoint() {
        return Mono.empty();
    }

    @GetMapping("/admin/check")
    public Mono<Void> adminEndpoint() {
        return Mono.empty();
    }

}

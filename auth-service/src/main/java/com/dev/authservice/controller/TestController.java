package com.dev.authservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class TestController {

    @PreAuthorize("hasRole('USER')")
    @RequestMapping("/test")
    public ResponseEntity<String> userEndpoint() {
        return ResponseEntity.ok("User ep");
    }

    @RequestMapping("/admin/test")
    public ResponseEntity<String> adminEndpoint() {
        return ResponseEntity.ok("ADmin ep");
    }
}

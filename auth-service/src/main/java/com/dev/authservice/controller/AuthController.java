package com.dev.authservice.controller;

import com.dev.authservice.dto.RegisterUserDto;
import com.dev.authservice.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> registerHandler(
            @Valid @RequestBody RegisterUserDto dto
    ) {
        log.info("Registering user: {}", dto);
        userService.register(dto);
        return ResponseEntity.ok("User registration initiated. Verify your email");
    }

    @GetMapping("/confirm")
    public ResponseEntity<String> confirmRegisterHandler(
            @RequestParam("userId") String userId
    ) {
        log.info("Verifying email for user with id: {}", userId);
        userService.confirmRegister(userId);

        return ResponseEntity.ok("Successfully confirmed");
    }

}

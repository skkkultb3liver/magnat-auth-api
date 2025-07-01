package com.dev.authservice.user;

import com.dev.authservice.dto.RegisterState;
import com.dev.authservice.dto.RegisterUserDto;
import com.dev.authservice.keycloak.KeycloakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final KeycloakService keycloakService;
    private final RedisTemplate<String, Object> redisTemplate;

    public void register(RegisterUserDto dto) {
        log.info("Registering user : {}", dto);

        if (keycloakService.isEmailExists(dto.email())) {
            throw new RuntimeException("Email already exists");
        }

        String userId = keycloakService.createUser(dto);
        keycloakService.sendEmailVerification(userId);

        log.info("Register confirmation message sent");

        RegisterState state = new RegisterState(userId, LocalDateTime.now());
        redisTemplate.opsForValue().set("registration:", state);
    }

    public void confirmRegister(String userId) {
        keycloakService.setEmailVerified(userId);
    }
}

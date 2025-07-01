package com.dev.authservice.cache;

import com.dev.authservice.dto.RegisterState;
import com.dev.authservice.exception.KeycloakException;
import com.dev.authservice.keycloak.KeycloakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegisterCleanUpService {

    private final KeycloakService keycloakService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Scheduled(fixedRate = 45000)
    public void cleanupExpiredRegistrations() {
        Set<String> keys = redisTemplate.keys("registration:*");

        if (keys != null) {
            for (String key : keys) {
                RegisterState state = (RegisterState) redisTemplate.opsForValue().get(key);

                log.info("Register state: {}", state);

                if (state != null) {

                    if (state.registerTime().isBefore(LocalDateTime.now().minusMinutes(4))) {

                        try {
                            log.info("Deleting expired registration for userid: {}", state.userId());
                            keycloakService.deleteUserById(state.userId());

                            log.info("Deleted expired registration for userid: {}", state.userId());
                        } catch (KeycloakException e) {
                            log.error("Error deleting expired registration for userid: {}", state.userId(), e);
                        } catch (Exception e) {
                            log.error("Unexpected exception: {}", e.getMessage());
                        } finally {
                            log.info("Deleting redis key for state: {}", state);
                            redisTemplate.delete(key);

                            log.info("Deleted redis key for state: {}", state);
                        }

                    }

                    if (keycloakService.getUserById(state.userId()) == null) {
                        redisTemplate.delete(key);
                        log.info("Deleted unexpected user state: {}", state.userId());
                    }

                    if (keycloakService.isEmailVerified(state.userId())) {
                        redisTemplate.delete(key);
                        log.info("Deleted email verified state: {}", state.userId());
                    }

                }
            }
        }
    }

}

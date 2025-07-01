package com.dev.authservice.keycloak;

import com.dev.authservice.dto.RegisterUserDto;
import com.dev.authservice.email.EmailService;
import com.dev.authservice.exception.KeycloakException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakService {

    private final RealmResource instance;
    private final EmailService emailService;

    public String createUser(RegisterUserDto dto) {
        log.info("Creating user dto: {}", dto);

        UserRepresentation user = createUserRepresentation(dto);

        log.info("Setup user representation and credentials: {}", dto.email());

        Response response = instance
                .users()
                .create(user);

        log.info("User created, status: {}, headers: {}", response.getStatus(), response.getHeaders());

        if (response.getStatus() != 201) {
            String errorBody = response.readEntity(String.class);
            log.error("Failed to create user, status: {}, response: {}", response.getStatus(), errorBody);
            throw new RuntimeException("Failed to create user in Keycloak, status: " + response.getStatus());
        }

        String locationHeader = response.getHeaderString("Location");
        return locationHeader.substring(locationHeader.lastIndexOf("/") + 1);
    }

    public void setEmailVerified(String userId) {

        if (!isEmailVerified(userId)) {
            UserResource userResource = instance.users().get(userId);
            UserRepresentation user = userResource.toRepresentation();
            user.setEmailVerified(true);

            userResource.update(user);
        }

    }

    public boolean isEmailVerified(String userId) {
        return getUserById(userId).isEmailVerified();
    }

    public UserRepresentation getUserById(String userId) {

        return instance.users()
                .get(userId)
                .toRepresentation();
    }

    public boolean isEmailExists(String email) {

        return !instance.users()
                .search(email)
                .isEmpty();
    }

    public void deleteUserById(String userId) {
        try {
            instance.users().delete(userId);
        } catch (Exception e) {
            log.error("Failed to delete user: {}", userId, e);
            throw new KeycloakException("Failed to delete user: " + userId);
        }
    }

    public void sendEmailVerification(String userId) {

        try {
            String appBaseUrl = "http://localhost:8222/api/v1/auth/confirm";
            String verifyUrl = String.format("%s?userId=%s", appBaseUrl, userId);

            UserResource userResource = instance.users().get(userId);
            String email = userResource.toRepresentation().getEmail();

            String emailBody = "Hello, click on the following link to confirm ur email (this action will be expired after 4 minutes): "
                    + verifyUrl;

            emailService.sendMessage(email, "Email verification", emailBody);

            log.info("Email verification sent to user with ID: {}", userId);

        } catch (Exception e) {
            log.error("Failed to send email verification for user ID: {}", userId, e);
            throw new RuntimeException("Unable to send email verification", e);
        }

    }

    private UserRepresentation createUserRepresentation(RegisterUserDto dto) {
        String username = dto.email().substring(0, dto.email().indexOf("@"));

        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(dto.email());
        user.setFirstName(dto.firstname());
        user.setLastName(dto.lastname());
        user.setEnabled(true);
        user.setEmailVerified(false);

        CredentialRepresentation creds = new CredentialRepresentation();
        creds.setType(CredentialRepresentation.PASSWORD);
        creds.setValue(dto.password());

        user.setCredentials(Collections.singletonList(creds));
        return user;
    }
}

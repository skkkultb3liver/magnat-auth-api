package com.dev.authservice.exception;

public class KeycloakException extends RuntimeException {
    public KeycloakException(String msg) {
        super(msg);
    }
}

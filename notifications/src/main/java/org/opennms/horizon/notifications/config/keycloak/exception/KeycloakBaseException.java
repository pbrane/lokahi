package org.opennms.horizon.notifications.config.keycloak.exception;

public class KeycloakBaseException extends Exception {
    public KeycloakBaseException() {
    }

    public KeycloakBaseException(String message) {
        super(message);
    }

    public KeycloakBaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeycloakBaseException(Throwable cause) {
        super(cause);
    }

    public KeycloakBaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

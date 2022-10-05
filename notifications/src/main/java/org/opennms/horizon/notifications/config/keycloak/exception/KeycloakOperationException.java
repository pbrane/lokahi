package org.opennms.horizon.notifications.config.keycloak.exception;

public class KeycloakOperationException extends KeycloakBaseException {
    public KeycloakOperationException() {
    }

    public KeycloakOperationException(String message) {
        super(message);
    }

    public KeycloakOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeycloakOperationException(Throwable cause) {
        super(cause);
    }

    public KeycloakOperationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

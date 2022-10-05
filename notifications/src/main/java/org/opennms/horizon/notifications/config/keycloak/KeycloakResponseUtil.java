package org.opennms.horizon.notifications.config.keycloak;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.opennms.horizon.notifications.config.keycloak.exception.KeycloakAuthenticationException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class KeycloakResponseUtil {

    public static final KeycloakResponseUtil INSTANCE = new KeycloakResponseUtil();

    private ObjectMapper objectMapper = new ObjectMapper();

//========================================
// Getters and Setters
//----------------------------------------

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

//========================================
// Operations
//----------------------------------------

    public AccessTokenResponse parseAccessTokenResponse(HttpResponse response) throws KeycloakAuthenticationException {
        return parseResponseCommon(response, AccessTokenResponse.class, "login request");
    }

    public UserRepresentation parseUserResponse(HttpResponse response) throws KeycloakAuthenticationException {
        return parseResponseCommon(response, UserRepresentation.class, "retrieve user");
    }

    public UserRepresentation[] parseUserArrayResponse(HttpResponse response) throws KeycloakAuthenticationException {
        return parseResponseCommon(response, UserRepresentation[].class, "retrieve user(s)");
    }

    public MappingsRepresentation parseMappingResponse(HttpResponse response) throws KeycloakAuthenticationException {
        return parseResponseCommon(response, MappingsRepresentation.class, "retrieve user role mappings");
    }

    public RoleRepresentation[] parseRoleArrayResponse(HttpResponse response) throws KeycloakAuthenticationException {
        return parseResponseCommon(response, RoleRepresentation[].class, "retrieve role by name");
    }

//========================================
// Internals
//----------------------------------------

    private <T> T parseResponseCommon(HttpResponse response, Class<T> clazz, String operationDescription) throws KeycloakAuthenticationException {
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == HttpStatus.SC_OK) {
            try (InputStream inputStream = response.getEntity().getContent()) {
                return objectMapper.readValue(inputStream, clazz);
            } catch (IOException ioException) {
                throw new KeycloakAuthenticationException("failed to parse response body", ioException);
            }
        } else {
            throw new KeycloakAuthenticationException(operationDescription + " status " + statusCode);
        }
    }
}

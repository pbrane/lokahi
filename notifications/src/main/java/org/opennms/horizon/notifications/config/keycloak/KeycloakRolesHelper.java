package org.opennms.horizon.notifications.config.keycloak;

import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class KeycloakRolesHelper {
    private final Logger LOG = LoggerFactory.getLogger(KeycloakRolesHelper.class);

    KeycloakRolesHelper(KeycloakAdminClient keycloakAdminClient) {
        this.keycloakAdminClient = keycloakAdminClient;
    }
    KeycloakAdminClient keycloakAdminClient;

    @Value("${horizon.keycloak.realm}")
    String keycloakRealm;

    public List<String> getRoles(String username) {
        List<String> roles = loadFromKeycloak(keycloakRealm, username);
        for (String role : roles) {
            LOG.warn("JH role="+ role);
        }
        return roles;
        //return Arrays.asList("user");
    }

    private List<String> loadFromKeycloak(String realm, String username) {
        KeycloakAdminClientSession session = null;
        try {
            session = this.keycloakAdminClient.login();
            UserRepresentation userRepresentation = session.getUserByUsername(realm, username);

            List<String> result;

            if (userRepresentation != null) {
                MappingsRepresentation mappingsRepresentation = session.getUserRoleMappings(realm, userRepresentation.getId());

                result = mappingsRepresentation.getRealmMappings().stream().map(RoleRepresentation::getName).collect(Collectors.toList());
            } else {
                LOG.warn("lookup of user {}: username not matched", username);
                result = Collections.EMPTY_LIST;
            }

            return result;
        } catch (Exception exc) {
            LOG.error("failed load user roles with realm {} for user {}", realm, username, exc);
            throw new RuntimeException("failed to load user roles from keycloak", exc);
        } finally {
            try {
                session.logout();
            } catch (Exception exc) {
                LOG.warn("failed to logout Keycloak session", exc);
            }
        }
    }

}

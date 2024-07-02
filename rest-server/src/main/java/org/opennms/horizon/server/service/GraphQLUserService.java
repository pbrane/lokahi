/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.horizon.server.service;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.opennms.horizon.server.config.KeyCloakConfig;
import org.opennms.horizon.server.model.users.ClientRepresentation;
import org.opennms.horizon.server.model.users.CredentialRepresentation;
import org.opennms.horizon.server.model.users.RealmRepresentation;
import org.opennms.horizon.server.model.users.SearchBy;
import org.opennms.horizon.server.model.users.UserRepresentation;
import org.opennms.horizon.server.utils.ServerHeaderUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@GraphQLApi
@Service
@RequiredArgsConstructor
@Slf4j
public class GraphQLUserService {

    private final KeyCloakConfig keyCloakConfig;

    private final ServerHeaderUtil headerUtil;

    private static final String ADMIN_REALMS_URI = "/admin/realms/";

    private static final String ADMIN_USERS_URI = "/users";

    private static final String RESET_PASSWORD = "/reset-password";

    private static final String EXACT_MATCH = "&exact=true";

    private static final String USERNAME_MATCH = "?username=";

    @GraphQLQuery(name = "getRealms")
    public Mono<RealmRepresentation> getRealms(@GraphQLEnvironment ResolutionEnvironment env) {
        // GET /admin/realms/{realm}
        String realmUrl = keyCloakConfig.getKeycloakUrl() + ADMIN_REALMS_URI + keyCloakConfig.getKeycloakRealm();
        var keycloakRealmsClient = WebClient.builder()
                .baseUrl(realmUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, headerUtil.getAuthHeader(env))
                .build();
        return keycloakRealmsClient.get().retrieve().bodyToMono(RealmRepresentation.class);
    }

    @GraphQLMutation(name = "addUser")
    public Mono<HttpStatus> addUser(
            @GraphQLArgument(name = "user") UserRepresentation user, @GraphQLEnvironment ResolutionEnvironment env) {

        if (Strings.isBlank(user.getUsername())) {
            throw new IllegalArgumentException("Username is null or empty");
        }
        if (user.getCredentials().isEmpty()) {
            throw new IllegalArgumentException("Credentials can't be empty");
        }
        // POST /admin/realms/{realm}/users
        String addUserUrl = keyCloakConfig.getKeycloakUrl()
                + ADMIN_REALMS_URI
                + keyCloakConfig.getKeycloakRealm()
                + ADMIN_USERS_URI;
        var keycloakUserClient = WebClient.builder()
                .baseUrl(addUserUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, headerUtil.getAuthHeader(env))
                .build();
        return keycloakUserClient
                .post()
                .bodyValue(user)
                .retrieve()
                .toBodilessEntity()
                .map(ResponseEntity::getStatusCode)
                .map(statusCode -> HttpStatus.valueOf(statusCode.value()));
    }

    @GraphQLMutation(name = "updateUser")
    public Mono<HttpStatus> updateUser(
            @GraphQLArgument(name = "user") UserRepresentation user, @GraphQLEnvironment ResolutionEnvironment env) {

        if (Strings.isBlank(user.getId())) {
            throw new IllegalArgumentException("User Id can't be empty for Update User");
        }
        if (Strings.isBlank(user.getUsername())) {
            throw new IllegalArgumentException("Username is null or empty");
        }
        // PUT /admin/realms/{realm}/users/{id}
        String addUserUrl = keyCloakConfig.getKeycloakUrl() + ADMIN_REALMS_URI + keyCloakConfig.getKeycloakRealm()
                + ADMIN_USERS_URI + "/" + user.getId();
        var keycloakUserClient = WebClient.builder()
                .baseUrl(addUserUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, headerUtil.getAuthHeader(env))
                .build();
        return keycloakUserClient
                .put()
                .bodyValue(user)
                .retrieve()
                .toBodilessEntity()
                .map(ResponseEntity::getStatusCode)
                .map(statusCode -> HttpStatus.valueOf(statusCode.value()));
    }

    @GraphQLMutation(name = "resetPassword")
    public Mono<HttpStatus> resetPassword(
            @GraphQLArgument(name = "userId") String userId,
            @GraphQLArgument(name = "credential") CredentialRepresentation credential,
            @GraphQLEnvironment ResolutionEnvironment env) {

        if (Strings.isBlank(userId)) {
            throw new IllegalArgumentException("User Id can't be empty for Update User");
        }
        if (credential == null) {
            throw new IllegalArgumentException("Credentials can't be null");
        }
        // PUT /admin/realms/{realm}/users/{id}/reset-password
        String addUserUrl = keyCloakConfig.getKeycloakUrl() + ADMIN_REALMS_URI + keyCloakConfig.getKeycloakRealm()
                + ADMIN_USERS_URI
                + "/" + userId + RESET_PASSWORD;
        var keycloakUserClient = WebClient.builder()
                .baseUrl(addUserUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, headerUtil.getAuthHeader(env))
                .build();
        return keycloakUserClient
                .put()
                .bodyValue(credential)
                .retrieve()
                .toBodilessEntity()
                .map(ResponseEntity::getStatusCode)
                .map(statusCode -> HttpStatus.valueOf(statusCode.value()));
    }

    @GraphQLQuery(name = "getUserByUsername")
    public Mono<UserRepresentation> getUserByUserName(
            @GraphQLArgument(name = "username") String username, @GraphQLEnvironment ResolutionEnvironment env) {

        // GET /admin/realms/{realm}/users?username={username}&exact=true
        String usersByUsernameUrl = keyCloakConfig.getKeycloakUrl()
                + ADMIN_REALMS_URI
                + keyCloakConfig.getKeycloakRealm()
                + ADMIN_USERS_URI
                + USERNAME_MATCH
                + username
                + EXACT_MATCH;
        var keycloakRealmsClient = WebClient.builder()
                .baseUrl(usersByUsernameUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, headerUtil.getAuthHeader(env))
                .build();
        return keycloakRealmsClient
                .get()
                .retrieve()
                .bodyToFlux(UserRepresentation.class)
                .single();
    }

    @GraphQLQuery(name = "getAllUsers")
    public Flux<UserRepresentation> getAllUsers(@GraphQLEnvironment ResolutionEnvironment env) {

        // GET /admin/realms/{realm}/users
        String allUsersUrl = keyCloakConfig.getKeycloakUrl()
                + ADMIN_REALMS_URI
                + keyCloakConfig.getKeycloakRealm()
                + ADMIN_USERS_URI;
        var keycloakRealmsClient = WebClient.builder()
                .baseUrl(allUsersUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, headerUtil.getAuthHeader(env))
                .build();
        return keycloakRealmsClient.get().retrieve().bodyToFlux(UserRepresentation.class);
    }

    @GraphQLQuery(name = "searchUsers")
    public Flux<UserRepresentation> searchUsers(
            @GraphQLArgument(name = "searchBy") String searchBy,
            @GraphQLArgument(name = "searchTerm") String searchTerm,
            @GraphQLEnvironment ResolutionEnvironment env) {

        if (Strings.isBlank(searchBy) || Strings.isBlank(searchTerm)) {
            throw new IllegalArgumentException("searchBy and searchTerm can't be empty");
        }
        validateSearchBy(searchBy);
        // GET /admin/realms/{realm}/users?searchBy={searchTerm}
        String usersByUsernameUrl = keyCloakConfig.getKeycloakUrl() + ADMIN_REALMS_URI
                + keyCloakConfig.getKeycloakRealm() + ADMIN_USERS_URI + "?" + searchBy + "=" + searchTerm;
        var keycloakRealmsClient = WebClient.builder()
                .baseUrl(usersByUsernameUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, headerUtil.getAuthHeader(env))
                .build();
        return keycloakRealmsClient.get().retrieve().bodyToFlux(UserRepresentation.class);
    }

    private void validateSearchBy(String searchBy) {
        try {
            SearchBy.valueOf(searchBy);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("searchBy : " + searchBy + " is not supported");
        }
    }

    @GraphQLQuery(name = "getClients")
    public Flux<ClientRepresentation> getClients(@GraphQLEnvironment ResolutionEnvironment env) {
        // GET /admin/realms/{realm}/clients
        String allUsersUrl =
                keyCloakConfig.getKeycloakUrl() + ADMIN_REALMS_URI + keyCloakConfig.getKeycloakRealm() + "/clients";
        var keycloakRealmsClient = WebClient.builder()
                .baseUrl(allUsersUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, headerUtil.getAuthHeader(env))
                .build();
        return keycloakRealmsClient.get().retrieve().bodyToFlux(ClientRepresentation.class);
    }
}

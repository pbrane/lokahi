package org.opennms.horizon.server.service;

import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@GraphQLApi
@RequiredArgsConstructor
public class GraphService {
    private final PlatformGateway gateway;

    @GraphQLQuery
    public String listContainerInfo(@GraphQLEnvironment ResolutionEnvironment env) {
        return gateway.get(String.format(PlatformGateway.URL_PATH_GRAPHS), gateway.getAuthHeader(env), String.class).getBody();
    }
}

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.horizon.server.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.opennms.horizon.server.mapper.AlertMapper;
import org.opennms.horizon.server.model.alerts.Alert;
import org.opennms.horizon.server.service.grpc.AlertsClient;
import org.opennms.horizon.server.utils.ServerHeaderUtil;
import org.springframework.stereotype.Service;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@GraphQLApi
@Service
public class GrpcAlertService {

    private final AlertsClient alertsClient;
    private final ServerHeaderUtil headerUtil;
    private final AlertMapper mapper;

//    @GraphQLQuery(name = "findAllAlerts" )
//    public Flux<Alert> findAllAlerts(@GraphQLEnvironment ResolutionEnvironment env) {
//        Alert alert = new Alert();
//        return Flux.fromIterable(List.of(alert));
//        return Flux.fromIterable(alertsClient.listAlerts(headerUtil.getAuthHeader(env)).stream().map(mapper::protoToAlert).toList());
//    }

    @GraphQLQuery
    public void findAllAlert(@GraphQLEnvironment ResolutionEnvironment env) {
    }

    @GraphQLQuery
    public Mono<Boolean> deleteAlert(@GraphQLArgument(name = "id") long id, @GraphQLEnvironment ResolutionEnvironment env) {
        return Mono.just(alertsClient.deleteAlert(id, headerUtil.getAuthHeader(env)));
    }
}

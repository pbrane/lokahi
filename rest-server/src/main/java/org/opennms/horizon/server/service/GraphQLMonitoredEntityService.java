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
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import lombok.RequiredArgsConstructor;
import org.opennms.horizon.server.mapper.MonitoredEntityMapper;
import org.opennms.horizon.server.mapper.MonitoredEntityStateMapper;
import org.opennms.horizon.server.model.inventory.MonitoredEntityDTO;
import org.opennms.horizon.server.model.inventory.MonitoredEntityState;
import org.opennms.horizon.server.model.inventory.MonitoredServiceStatusRequest;
import org.opennms.horizon.server.service.grpc.InventoryClient;
import org.opennms.horizon.server.utils.ServerHeaderUtil;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@GraphQLApi
@Service
public class GraphQLMonitoredEntityService {

    private final ServerHeaderUtil headerUtil;
    private final MonitoredEntityStateMapper mapper;
    private final InventoryClient client;
    private final MonitoredEntityMapper monitoredEntityMapper;

    @GraphQLQuery(name = "getMonitoredEntityState")
    public Mono<MonitoredEntityState> getMonitoredEntityState(
            @GraphQLArgument(name = "request") MonitoredServiceStatusRequest request,
            @GraphQLEnvironment ResolutionEnvironment env) {
        var monitoredEntityStateProto = client.getMonitoredEntityState(request, headerUtil.getAuthHeader(env));
        var monitoredEntityState = mapper.protoToModel(monitoredEntityStateProto);
        return Mono.just(monitoredEntityState);
    }

    @GraphQLQuery(name = "findAllMonitoredEntitiesByLocation")
    public Flux<MonitoredEntityDTO> findAllMonitoredEntitiesByLocation(
            @GraphQLArgument(name = "locationId") long locationId, @GraphQLEnvironment ResolutionEnvironment env) {
        return Flux.fromStream(
                client.getAllMonitoredEntitiesByLocation(locationId, headerUtil.getAuthHeader(env)).stream()
                        .map(monitoredEntityMapper::protoToModel));
    }

    @GraphQLQuery(name = "findMonitoredEntitiesBySearchTerm")
    public Flux<MonitoredEntityDTO> findMonitoredEntitiesBySearchTerm(
            @GraphQLArgument(name = "locationId") long locationId,
            @GraphQLArgument(name = "providerId") String providerId,
            @GraphQLEnvironment ResolutionEnvironment env) {
        return Flux.fromStream(
                client
                        .getAllMonitoredEntitiesBySearchTerm(locationId, providerId, headerUtil.getAuthHeader(env))
                        .stream()
                        .map(monitoredEntityMapper::protoToModel));
    }

    @GraphQLQuery(name = "findAllMonitoredEntities")
    public Flux<MonitoredEntityDTO> findAllMonitoredEntities(@GraphQLEnvironment ResolutionEnvironment env) {
        return Flux.fromStream(client.getAllMonitoredEntities(headerUtil.getAuthHeader(env)).stream()
                .map(monitoredEntityMapper::protoToModel));
    }
}

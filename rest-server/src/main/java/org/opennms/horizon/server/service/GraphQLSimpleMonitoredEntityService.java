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
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.opennms.horizon.server.mapper.MonitoredEntityStateMapper;
import org.opennms.horizon.server.mapper.SimpleMonitoredEntityMapper;
import org.opennms.horizon.server.model.inventory.MonitoredEntityState;
import org.opennms.horizon.server.model.inventory.SimpleMonitoredEntity;
import org.opennms.horizon.server.service.grpc.InventoryClient;
import org.opennms.horizon.server.utils.ServerHeaderUtil;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@GraphQLApi
@Service
public class GraphQLSimpleMonitoredEntityService {

    private final InventoryClient inventoryClient;
    private final ServerHeaderUtil headerUtil;
    private final SimpleMonitoredEntityMapper mapper;
    private final MonitoredEntityStateMapper stateMapper;

    @GraphQLQuery(name = "getAllSimpleMonitoredEntities")
    public Flux<SimpleMonitoredEntity> getAll(@GraphQLEnvironment ResolutionEnvironment env) {
        return Flux.fromIterable(
                this.inventoryClient.getAllSimpleMonitoredEntities(this.headerUtil.getAuthHeader(env)).stream()
                        .map(this.mapper::toTransport)
                        .toList());
    }

    @GraphQLMutation(name = "upsertSimpleMonitoredEntity")
    public Mono<SimpleMonitoredEntity> update(
            @GraphQLArgument(name = "entity") SimpleMonitoredEntity entity,
            @GraphQLEnvironment ResolutionEnvironment env) {
        return Mono.just(this.mapper.toTransport(this.inventoryClient.upsertSimpleMonitoredEntity(
                this.mapper.toRequest(entity), this.headerUtil.getAuthHeader(env))));
    }

    @GraphQLMutation(name = "deleteSimpleMonitoredEntity")
    public Mono<Boolean> delete(@GraphQLArgument(name = "id") UUID id, @GraphQLEnvironment ResolutionEnvironment env) {
        return Mono.just(
                this.inventoryClient.deleteSimpleMonitoredEntity(id.toString(), this.headerUtil.getAuthHeader(env)));
    }

    @GraphQLQuery(name = "listAllSimpleMonitors")
    public Flux<MonitoredEntityState> listAllSimpleMonitors(@GraphQLEnvironment ResolutionEnvironment env) {
        return Flux.fromIterable(inventoryClient.listAllSimpleMonitors(headerUtil.getAuthHeader(env)).stream()
                .map(stateMapper::protoToModel)
                .toList());
    }
}

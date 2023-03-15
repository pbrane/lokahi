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

import com.google.protobuf.BoolValue;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import lombok.RequiredArgsConstructor;
import org.opennms.horizon.alerts.proto.AlertDefinition;
import org.opennms.horizon.alerts.proto.ListAlertDefinitionsRequest;
import org.opennms.horizon.alerts.proto.ListAlertDefinitionsResponse;
import org.opennms.horizon.inventory.dto.PassiveDiscoveryDTO;
import org.opennms.horizon.inventory.dto.PassiveDiscoveryUpsertDTO;
import org.opennms.horizon.server.mapper.AlertDefinitionDTOMapper;
import org.opennms.horizon.server.model.alert.AlertDefinitionDTO;
import org.opennms.horizon.server.model.alert.ListAlertDefinitionsRequestDTO;
import org.opennms.horizon.server.model.alert.ListAlertDefinitionsResponseDTO;
import org.opennms.horizon.server.model.inventory.discovery.ActiveDiscovery;
import org.opennms.horizon.server.model.inventory.discovery.passive.PassiveDiscovery;
import org.opennms.horizon.server.model.inventory.discovery.passive.PassiveDiscoveryUpsert;
import org.opennms.horizon.server.service.grpc.AlertsClient;
import org.opennms.horizon.server.utils.ServerHeaderUtil;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@GraphQLApi
@Service
public class GrpcAlertConfigurationService {
    private final AlertsClient client;
    private final AlertDefinitionDTOMapper mapper;
    private final ServerHeaderUtil headerUtil;

    @GraphQLQuery
    public Mono<ListAlertDefinitionsResponseDTO> listAlertDefinitions(ListAlertDefinitionsRequestDTO request, @GraphQLEnvironment ResolutionEnvironment env) {
        return Mono.just(mapper.protoToListAlertDefinitionsResponseDTO(client.listAlertDefinitions(mapper.listAlertDefinitionsRequestDTOtoProto(request), headerUtil.getAuthHeader(env))));
    }

    @GraphQLQuery
    public Mono<AlertDefinitionDTO> getAlertDefinition(long id, @GraphQLEnvironment ResolutionEnvironment env) {
        return Mono.just(mapper.protoToAlertDefinitionDTO(client.getAlertDefinition(mapper.longToUInt64Value(id), headerUtil.getAuthHeader(env))));
    }

    @GraphQLMutation
    public Mono<AlertDefinitionDTO> insertAlertDefinition(AlertDefinitionDTO alert,
                                                         @GraphQLEnvironment ResolutionEnvironment env) {
        String authHeader = headerUtil.getAuthHeader(env);
        AlertDefinition insertAlert = mapper.alertDefinitionDTOToProto(alert);
        AlertDefinition dto = client.insertAlertDefinition(insertAlert, authHeader);
        return Mono.just(mapper.protoToAlertDefinitionDTO(dto));
    }

    @GraphQLMutation
    public Mono<AlertDefinitionDTO> updateAlertDefinition(AlertDefinitionDTO alert,
                                                          @GraphQLEnvironment ResolutionEnvironment env) {
        String authHeader = headerUtil.getAuthHeader(env);
        AlertDefinition updateAlert = mapper.alertDefinitionDTOToProto(alert);
        AlertDefinition dto = client.updateAlertDefinition(updateAlert, authHeader);
        return Mono.just(mapper.protoToAlertDefinitionDTO(dto));
    }

    @GraphQLMutation
    public Mono<BoolValue> removeAlertDefinition(long id,
                                                          @GraphQLEnvironment ResolutionEnvironment env) {
        String authHeader = headerUtil.getAuthHeader(env);
        BoolValue result = client.removeAlertDefinition(mapper.longToUInt64Value(id), authHeader);
        return Mono.just(result);
    }
}

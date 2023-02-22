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

import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import lombok.RequiredArgsConstructor;
import org.opennms.horizon.inventory.dto.PassiveDiscoveryCreateDTO;
import org.opennms.horizon.inventory.dto.PassiveDiscoveryDTO;
import org.opennms.horizon.inventory.dto.PassiveDiscoveryResponseDTO;
import org.opennms.horizon.server.mapper.PassiveDiscoveryMapper;
import org.opennms.horizon.server.model.inventory.discovery.passive.PassiveDiscovery;
import org.opennms.horizon.server.model.inventory.discovery.passive.PassiveDiscoveryCreate;
import org.opennms.horizon.server.service.grpc.InventoryClient;
import org.opennms.horizon.server.utils.ServerHeaderUtil;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@GraphQLApi
@Service
public class GrpcPassiveDiscoveryService {
    private final InventoryClient client;
    private final PassiveDiscoveryMapper mapper;
    private final ServerHeaderUtil headerUtil;

    @GraphQLMutation
    public Mono<PassiveDiscovery> createPassiveDiscovery(PassiveDiscoveryCreate discovery,
                                                         @GraphQLEnvironment ResolutionEnvironment env) {
        String authHeader = headerUtil.getAuthHeader(env);
        PassiveDiscoveryCreateDTO createDto = mapper.discoveryCreateToProtoCustom(discovery);
        PassiveDiscoveryDTO dto = client.createPassiveDiscovery(createDto, authHeader);
        return Mono.just(mapper.protoToDiscovery(dto));
    }

    @GraphQLQuery
    public Mono<PassiveDiscovery> getPassiveDiscovery(@GraphQLEnvironment ResolutionEnvironment env) {
        PassiveDiscoveryResponseDTO response = client.getPassiveDiscovery(headerUtil.getAuthHeader(env));
        if (response.hasDiscovery()) {
            return Mono.just(mapper.protoToDiscovery(response.getDiscovery()));
        }
        return Mono.empty();
    }
}

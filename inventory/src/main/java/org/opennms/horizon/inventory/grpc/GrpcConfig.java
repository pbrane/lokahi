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

package org.opennms.horizon.inventory.grpc;

import java.util.Arrays;

import org.opennms.horizon.inventory.service.MonitoringLocationService;
import org.opennms.horizon.inventory.service.MonitoringSystemService;
import lombok.RequiredArgsConstructor;
import org.opennms.horizon.inventory.mapper.MonitoringLocationMapper;
import org.opennms.horizon.inventory.mapper.NodeMapper;
import org.opennms.horizon.inventory.repository.IpInterfaceRepository;
import org.opennms.horizon.inventory.repository.MonitoringLocationRepository;
import org.opennms.horizon.inventory.repository.NodeRepository;
import org.opennms.horizon.inventory.service.grpc.DeviceGrpcService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class GrpcConfig {
    private static final int DEFAULT_GRPC_PORT = 8990;
    private final MonitoringSystemService systemService;
    private final MonitoringLocationService locationService;
    @Value("${grpc.server.port:" + DEFAULT_GRPC_PORT +"}")
    private int port;
    private final MonitoringLocationRepository locationRepo;
    private final MonitoringLocationMapper mapper;
    private final NodeRepository nodeRepository;
    private final MonitoringLocationRepository monitoringLocationRepository;
    private final IpInterfaceRepository ipInterfaceRepository;
    private final NodeMapper nodeMapper;


    @Bean
    public TenantLookup createTenantLookup(){
        return new GrpcTenantLookupImpl();
    }

    @Bean
    public MonitoringLocationGrpcService createLocationGrpcService(TenantLookup tenantLookup) {
        return new MonitoringLocationGrpcService(locationService, tenantLookup);
    }

    @Bean
    public MonitoringSystemGrpcService createSystemGrpcService(TenantLookup tenantLookup) {
        return new MonitoringSystemGrpcService(systemService, tenantLookup);
    }

    @Bean
    public DeviceGrpcService createDeviceService() {
        return new DeviceGrpcService(nodeRepository, monitoringLocationRepository, ipInterfaceRepository, nodeMapper);
    }

    @Bean(destroyMethod = "stopServer")
    public GrpcServerManager startServer(MonitoringLocationGrpcService locationGrpc, MonitoringSystemGrpcService systemGrpc, DeviceGrpcService deviceGrpcService) {
        GrpcServerManager manager = new GrpcServerManager(port);
        manager.startServer(Arrays.asList(locationGrpc, systemGrpc, deviceGrpcService));
        return manager;
    }
}

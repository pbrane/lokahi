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

package org.opennms.horizon.inventory.service.discovery;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.opennms.horizon.inventory.dto.PassiveDiscoveryCreateDTO;
import org.opennms.horizon.inventory.dto.PassiveDiscoveryDTO;
import org.opennms.horizon.inventory.dto.TagCreateListDTO;
import org.opennms.horizon.inventory.exception.InventoryRuntimeException;
import org.opennms.horizon.inventory.mapper.PassiveDiscoveryMapper;
import org.opennms.horizon.inventory.model.MonitoringLocation;
import org.opennms.horizon.inventory.model.PassiveDiscovery;
import org.opennms.horizon.inventory.repository.MonitoringLocationRepository;
import org.opennms.horizon.inventory.repository.discovery.passive.PassiveDiscoveryRepository;
import org.opennms.horizon.inventory.service.ConfigUpdateService;
import org.opennms.horizon.inventory.service.TagService;
import org.opennms.horizon.shared.constants.GrpcConstants;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PassiveDiscoveryService {
    private final PassiveDiscoveryMapper mapper;
    private final PassiveDiscoveryRepository repository;
    private final MonitoringLocationRepository locationRepository;
    private final ConfigUpdateService configUpdateService;
    private final TagService tagService;

    @Transactional
    public PassiveDiscoveryDTO createDiscovery(String tenantId, PassiveDiscoveryCreateDTO request) {
        validateSnmpPorts(request);

        Optional<PassiveDiscovery> discoveryOpt = repository.findByTenantId(tenantId);
        if (discoveryOpt.isPresent()) {
            throw new NotImplementedException("Update of passive discovery not implemented yet");
        } else {
            PassiveDiscovery discovery = mapper.dtoToModel(request);

            discovery.setTenantId(tenantId);
            discovery.setToggle(true);
            discovery.setCreateTime(LocalDateTime.now());

            discovery = repository.save(discovery);

            List<MonitoringLocation> monitoringLocations = getMonitoringLocations(tenantId, request);
            discovery.getMonitoringLocations().addAll(monitoringLocations);

            tagService.addTags(tenantId, TagCreateListDTO.newBuilder()
                .setPassiveDiscoveryId(discovery.getId())
                .addAllTags(request.getTagsList())
                .build());

            return mapper.modelToDtoCustom(discovery);
        }
    }

    @Transactional(readOnly = true)
    public Optional<PassiveDiscoveryDTO> getPassiveDiscovery(String tenantId) {
        Optional<PassiveDiscovery> discoveryOpt = repository.findByTenantId(tenantId);
        return discoveryOpt.map(mapper::modelToDtoCustom);
    }

    private void validateSnmpPorts(PassiveDiscoveryCreateDTO dto) {
        List<Integer> snmpPorts = dto.getPortsList();
        for (Integer port : snmpPorts) {
            if (port < Constants.SNMP_PORT_MIN || port > Constants.SNMP_PORT_MAX) {
                String message = String.format("SNMP port is not in range [%d,%d] with value: %d",
                    Constants.SNMP_PORT_MIN, Constants.SNMP_PORT_MAX, port);
                throw new InventoryRuntimeException(message);
            }
        }
    }

    private List<MonitoringLocation> getMonitoringLocations(String tenantId, PassiveDiscoveryCreateDTO request) {
        List<MonitoringLocation> monitoringLocations = new ArrayList<>();
        for (String location : request.getLocationsList()) {

            location = StringUtils.isEmpty(location)
                ? GrpcConstants.DEFAULT_LOCATION : location;

            if (isLocationInList(location, monitoringLocations)) {
                continue;
            }

            Optional<MonitoringLocation> locationOp = locationRepository
                .findByLocationAndTenantId(location, tenantId);

            if (locationOp.isPresent()) {
                monitoringLocations.add(locationOp.get());
            } else {
                MonitoringLocation monitoringLocation = new MonitoringLocation();
                monitoringLocation.setLocation(location);
                monitoringLocation.setTenantId(tenantId);
                monitoringLocation = locationRepository.save(monitoringLocation);

                // Send config updates asynchronously to Minion
                configUpdateService.sendConfigUpdate(tenantId, location);

                monitoringLocations.add(monitoringLocation);
            }
        }
        return monitoringLocations;
    }

    private boolean isLocationInList(String location, List<MonitoringLocation> monitoringLocations) {
        for (MonitoringLocation monitoringLocation : monitoringLocations) {
            if (monitoringLocation.getLocation().equals(location)) {
                return true;
            }
        }
        return false;
    }
}

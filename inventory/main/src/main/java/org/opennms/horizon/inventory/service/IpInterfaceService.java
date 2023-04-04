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

package org.opennms.horizon.inventory.service;


import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.opennms.horizon.inventory.dto.IpInterfaceDTO;
import org.opennms.horizon.inventory.mapper.IpInterfaceMapper;
import org.opennms.horizon.inventory.model.IpInterface;
import org.opennms.horizon.inventory.model.node.Node;
import org.opennms.horizon.inventory.model.SnmpInterface;
import org.opennms.horizon.inventory.repository.IpInterfaceRepository;
import org.opennms.horizon.shared.utils.InetAddressUtils;
import org.opennms.node.scan.contract.IpInterfaceResult;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IpInterfaceService {
    private final IpInterfaceRepository repository;
    private final IpInterfaceMapper mapper;

    public void saveIpInterface(String tenantId, Node node, String ipAddress) {
        IpInterface ipInterface = new IpInterface();
        ipInterface.setNode(node);
        ipInterface.setTenantId(tenantId);
        ipInterface.setIpAddress(InetAddressUtils.getInetAddress(ipAddress));
        ipInterface.setSnmpPrimary(true);
        repository.save(ipInterface);
        node.getIpInterfaces().add(ipInterface);
    }

    public List<IpInterfaceDTO> findByTenantId(String tenantId) {
        List<IpInterface> all = repository.findByTenantId(tenantId);
        return all
            .stream()
            .map(mapper::modelToDTO)
            .toList();
    }

    public Optional<IpInterfaceDTO> getByIdAndTenantId(long id, String tenantId) {
        return repository.findByIdAndTenantId(id, tenantId).map(mapper::modelToDTO);
    }

    public Optional<IpInterfaceDTO> findByIpAddressAndLocationAndTenantId(String ipAddress, String location, String tenantId) {
        Optional<IpInterface> optional = repository.findByIpAddressAndLocationAndTenantId(InetAddressUtils.getInetAddress(ipAddress), location, tenantId);
        return optional.map(mapper::modelToDTO);
    }


    public void createOrUpdateFromScanResult(String tenantId, Node node, IpInterfaceResult result, Map<Integer, SnmpInterface> ifIndexSNMPMap) {
        repository.findByNodeIdAndTenantIdAndIpAddress(node.getId(), tenantId, InetAddressUtils.getInetAddress(result.getIpAddress()))
            .ifPresentOrElse(ipInterface -> {
                ipInterface.setHostname(result.getIpHostName());
                ipInterface.setNetmask(result.getNetmask());
                var snmpInterface = ifIndexSNMPMap.get(result.getIfIndex());
                if(snmpInterface != null) {
                    ipInterface.setSnmpInterface(snmpInterface);
                }
                ipInterface.setIfIndex(result.getIfIndex());
                repository.save(ipInterface);
            }, () -> {
                IpInterface ipInterface = mapper.fromScanResult(result);
                ipInterface.setNode(node);
                ipInterface.setTenantId(tenantId);
                ipInterface.setSnmpPrimary(false);
                ipInterface.setHostname(result.getIpHostName());
                ipInterface.setIfIndex(result.getIfIndex());
                var snmpInterface = ifIndexSNMPMap.get(result.getIfIndex());
                if(snmpInterface != null) {
                    ipInterface.setSnmpInterface(snmpInterface);
                }
                repository.save(ipInterface);
            });
    }
}

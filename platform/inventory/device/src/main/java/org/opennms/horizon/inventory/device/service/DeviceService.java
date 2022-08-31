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

package org.opennms.horizon.inventory.device.service;

import java.util.Date;
import java.util.List;

import com.google.common.base.Strings;
import org.opennms.horizon.db.dao.api.IpInterfaceDao;
import org.opennms.horizon.db.dao.api.MonitoringLocationDao;
import org.opennms.horizon.db.model.OnmsIpInterface;
import org.opennms.horizon.db.model.OnmsMonitoringLocation;
import org.opennms.horizon.db.model.OnmsNode;
import org.opennms.horizon.shared.dto.device.DeviceCollectionDTO;
import org.opennms.horizon.shared.dto.device.DeviceCreateDTO;
import org.opennms.horizon.shared.dto.device.DeviceDTO;

public class DeviceService extends AbstractService<OnmsNode, DeviceDTO, Integer> {

    private MonitoringLocationDao locationDao;

    private IpInterfaceDao ipInterfaceDao;

    public DeviceCollectionDTO searchDevices() {
        List<DeviceDTO> deviceDTOS = findAll();
        deviceDTOS.forEach(this::setManagementIp);
        return new DeviceCollectionDTO(deviceDTOS);
    }

    public DeviceDTO getDevice(Integer id) {
        DeviceDTO deviceDTO = getById(id);
        setManagementIp(deviceDTO);
        return deviceDTO;
    }

    private void setManagementIp(DeviceDTO deviceDTO) {

        String managementIp = sessionUtils.withReadOnlyTransaction(() -> {
            List<OnmsIpInterface> ipInterfaces = ipInterfaceDao.findInterfacesByNodeId(deviceDTO.getId());
            if (!ipInterfaces.isEmpty()) {
                OnmsIpInterface onmsIpInterface = ipInterfaces.get(0);
                return onmsIpInterface.getIpAddress().getHostAddress();
            }
            return null;
        });
        if (managementIp != null) {
            deviceDTO.setManagementIp(managementIp);
        }
    }

    public Integer createDevice(DeviceCreateDTO newDevice) {
        // Retrieve existing location
        var monitoringLocation = sessionUtils.withReadOnlyTransaction(() -> {
            if (Strings.isNullOrEmpty(newDevice.getLocation())) {
                return locationDao.getDefaultLocation();
            } else {
                return locationDao.get(newDevice.getLocation());
            }
        });
        // If no existing location, create one.
        if (monitoringLocation == null) {
            monitoringLocation = new OnmsMonitoringLocation(newDevice.getLocation(), newDevice.getMonitoringArea());
        }
        if (newDevice.getLatitude() != null) {
            monitoringLocation.setLatitude(newDevice.getLatitude().floatValue());
        }
        if (newDevice.getLongitude() != null) {
            monitoringLocation.setLongitude(newDevice.getLongitude().floatValue());
        }
        // Persist location
        final var updatedLocation = monitoringLocation;
        sessionUtils.withTransaction(() -> locationDao.saveOrUpdate(updatedLocation));

        // TODO: Derive existing node, for now always create new node.

        OnmsNode onmsNode = new OnmsNode(updatedLocation, newDevice.getLabel());
        if (newDevice.getManagementIp() != null) {
            OnmsIpInterface onmsIpInterface = new OnmsIpInterface(newDevice.getManagementIp());
            onmsNode.addIpInterface(onmsIpInterface);
        }
        onmsNode.setCreateTime(new Date());
        return createEntity(onmsNode);
    }

    public void setLocationDao(MonitoringLocationDao locationDao) {
        this.locationDao = locationDao;
    }

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        this.ipInterfaceDao = ipInterfaceDao;
    }
}

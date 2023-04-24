/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022-2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.horizon.minion.taskset.worker.impl;

import org.opennms.horizon.minion.plugin.api.MonitoredService;

import java.net.InetAddress;

// TODO: why an interface - this seems like a straight-forward model?
public class GeneralMonitoredService implements MonitoredService {

    private final String svcName;
    private final String ipAddr;
    private final long nodeId;
    private final String nodeLabel;
    private final String nodeLocation;
    private final InetAddress address;

    public GeneralMonitoredService(String svcName, String ipAddr, long nodeId, String nodeLabel, String nodeLocation, InetAddress address) {
        this.svcName = svcName;
        this.ipAddr = ipAddr;
        this.nodeId = nodeId;
        this.nodeLabel = nodeLabel;
        this.nodeLocation = nodeLocation;
        this.address = address;
    }

    @Override
    public String getSvcName() {
        return svcName;
    }

    @Override
    public String getIpAddr() {
        return ipAddr;
    }

    @Override
    public long getNodeId() {
        return nodeId;
    }

    @Override
    public String getNodeLabel() {
        return nodeLabel;
    }

    @Override
    public String getNodeLocation() {
        return nodeLocation;
    }

    @Override
    public InetAddress getAddress() {
        return address;
    }
}

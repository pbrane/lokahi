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

package org.opennms.horizon.inventory.component;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opennms.cloud.grpc.minion.Identity;
import org.opennms.horizon.grpc.heartbeat.contract.HeartbeatMessage;
import org.opennms.horizon.inventory.compnent.MinionHeartbeatConsumer;
import org.opennms.horizon.inventory.model.MonitoringLocation;
import org.opennms.horizon.inventory.model.MonitoringSystem;
import org.opennms.horizon.inventory.repository.MonitoringLocationRepository;
import org.opennms.horizon.inventory.repository.MonitoringSystemRepository;

public class MinionHeartbeatConsumerTest {
    private MonitoringLocationRepository mockLocationRepo;
    private MonitoringSystemRepository mockMonitoringSystemRepo;
    private MinionHeartbeatConsumer consumer;

    private MonitoringSystem testMonitoringSystem;
    private MonitoringLocation testLocation;

    private HeartbeatMessage heartbeatMessage;
    private final String location = "test location";
    private final String systemId = "test-monitoring-system-12345";

    @BeforeEach
    public void setUP(){
        mockLocationRepo = mock(MonitoringLocationRepository.class);
        mockMonitoringSystemRepo = mock(MonitoringSystemRepository.class);
        consumer = new MinionHeartbeatConsumer(mockMonitoringSystemRepo, mockLocationRepo);
        testLocation = new MonitoringLocation();
        testMonitoringSystem = new MonitoringSystem();
        heartbeatMessage = HeartbeatMessage.newBuilder()
            .setIdentity(Identity.newBuilder()
                .setLocation(location)
                .setSystemId(systemId).build()).build();
    }

    @AfterEach
    public void postTest() {
        verifyNoMoreInteractions(mockLocationRepo);
        verifyNoMoreInteractions(mockMonitoringSystemRepo);
    }

    @Test
    public void testReceiveMsgMonitorSystemExist() {
        doReturn(Optional.of(testMonitoringSystem)).when(mockMonitoringSystemRepo).findMonitoringSystemBySystemId(systemId);
        consumer.receiveMessage(heartbeatMessage.toByteArray());
        verify(mockMonitoringSystemRepo).findMonitoringSystemBySystemId(systemId);
        verify(mockMonitoringSystemRepo).save(testMonitoringSystem);
    }

    @Test
    public void testCreateNewMonitorSystemWithLocationExist() {
        doReturn(Optional.empty()).when(mockMonitoringSystemRepo).findMonitoringSystemBySystemId(systemId);
        doReturn(Optional.of(testLocation)).when(mockLocationRepo).findMonitoringLocationByLocation(location);
        consumer.receiveMessage(heartbeatMessage.toByteArray());
        verify(mockMonitoringSystemRepo).findMonitoringSystemBySystemId(systemId);
        verify(mockMonitoringSystemRepo).save(any(MonitoringSystem.class));
        verify(mockLocationRepo).findMonitoringLocationByLocation(location);
    }

    @Test
    public void testCreateNewMonitorSystemAndNewLocation() {
        doReturn(Optional.empty()).when(mockMonitoringSystemRepo).findMonitoringSystemBySystemId(systemId);
        doReturn(Optional.empty()).when(mockLocationRepo).findMonitoringLocationByLocation(location);
        consumer.receiveMessage(heartbeatMessage.toByteArray());
        verify(mockMonitoringSystemRepo).findMonitoringSystemBySystemId(systemId);
        verify(mockMonitoringSystemRepo).save(any(MonitoringSystem.class));
        verify(mockLocationRepo).findMonitoringLocationByLocation(location);
        verify(mockLocationRepo).save(any(MonitoringLocation.class));
    }
}

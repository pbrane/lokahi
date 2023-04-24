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

package org.opennms.horizon.minion.icmp;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;

import com.google.protobuf.Any;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opennms.horizon.shared.icmp.PingerFactory;
import org.opennms.icmp.contract.IcmpMonitorRequest;
import org.opennms.horizon.shared.utils.InetAddressUtils;
import org.opennms.horizon.minion.plugin.api.MonitoredService;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorResponse;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorResponse.Status;

public class IcmpMonitorTest {
    private static final String TEST_LOCALHOST_IP_VALUE = "127.0.0.1";
    @Mock
    MonitoredService monitoredService;

    IcmpMonitorRequest testEchoRequest;
    Any testConfig;
    IcmpMonitor icmpMonitor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        TestPinger testPinger = new TestPinger();
        testPinger.setHandleResponse(true);

        PingerFactory pingerFactory = Mockito.mock(PingerFactory.class);
        when(pingerFactory.getInstance(anyInt(), anyBoolean()))
            .thenReturn(testPinger);

        icmpMonitor = new IcmpMonitor(pingerFactory);

        testEchoRequest =
            IcmpMonitorRequest.newBuilder()
                .setHost(TEST_LOCALHOST_IP_VALUE)
                .build();

        testConfig = Any.pack(testEchoRequest);
    }

    @Test
    public void poll() throws Exception {
        CompletableFuture<ServiceMonitorResponse> response = icmpMonitor.poll(monitoredService, testConfig);

        ServiceMonitorResponse serviceMonitorResponse = response.get();

        assertEquals(Status.Up, serviceMonitorResponse.getStatus());
        assertTrue(serviceMonitorResponse.getResponseTime() > 0.0);
    }
}

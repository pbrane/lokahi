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
package org.opennms.horizon.minion.icmp.shell;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.protobuf.Message;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opennms.horizon.minion.plugin.api.ServiceMonitor;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorManager;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorResponse;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorResponseImpl;
import org.opennms.horizon.minion.plugin.api.registries.MonitorRegistry;
import org.opennms.monitors.http.contract.HttpMonitorRequest;
import org.opennms.monitors.ntp.contract.NTPMonitorRequest;
import org.opennms.ssh.contract.SshMonitorRequest;

class MonitorsCommandTest {

    @Mock
    private MonitorRegistry monitorRegistry;

    @InjectMocks
    private MonitorCommand monitorCommand;

    @Mock
    CompletableFuture<ServiceMonitorResponse> future;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testNtpMonitor() throws Exception {
        String monitorName = "NTPMonitor";
        String jsonString = "{\"inet_address\":\"time2.google.com\",\"port\":[123]}";
        ServiceMonitorManager realManager = mock(ServiceMonitorManager.class);
        when(monitorRegistry.getService(monitorName)).thenReturn(realManager);
        ServiceMonitor monitor = mock(ServiceMonitor.class);
        when(realManager.create()).thenReturn(monitor);
        Message.Builder request = NTPMonitorRequest.newBuilder();
        when(realManager.createRequestBuilder()).thenReturn(request);
        ServiceMonitorResponseImpl response = mock(ServiceMonitorResponseImpl.class);
        when(response.getStatus()).thenReturn(ServiceMonitorResponse.Status.Up);
        when(monitor.poll(any())).thenReturn(future);
        when(future.get(anyLong(), any())).thenReturn(response);

        try {
            FieldUtils.writeField(monitorCommand, "monitorName", monitorName, true);
            FieldUtils.writeField(monitorCommand, "request", jsonString, true);
            FieldUtils.writeField(monitorCommand, "monitorRegistry", monitorRegistry, true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        Object result = monitorCommand.execute();
        Assertions.assertEquals(ServiceMonitorResponse.Status.Up, result);
    }

    @Test
    void testHttpMonitor() throws Exception {

        String monitorName = "HTTPMonitor";
        String jsonString =
                "{\"inet_address\":\"www.google.com\",\"response_code\":\"200-500\",\"url\":\"https://www.google.com/\",\"ports\":{\"port\":[80]}}";

        ServiceMonitorManager realManager = mock(ServiceMonitorManager.class);
        when(monitorRegistry.getService(monitorName)).thenReturn(realManager);
        ServiceMonitor monitor = mock(ServiceMonitor.class);
        when(realManager.create()).thenReturn(monitor);
        Message.Builder request = HttpMonitorRequest.newBuilder();
        when(realManager.createRequestBuilder()).thenReturn(request);
        ServiceMonitorResponseImpl response = mock(ServiceMonitorResponseImpl.class);
        when(response.getStatus()).thenReturn(ServiceMonitorResponse.Status.Up);
        when(monitor.poll(any())).thenReturn(future);
        when(future.get(anyLong(), any())).thenReturn(response);

        try {
            FieldUtils.writeField(monitorCommand, "monitorName", monitorName, true);
            FieldUtils.writeField(monitorCommand, "request", jsonString, true);
            FieldUtils.writeField(monitorCommand, "monitorRegistry", monitorRegistry, true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        Object result = monitorCommand.execute();
        Assertions.assertEquals(ServiceMonitorResponse.Status.Up, result);
    }

    @Test
    void testHttpsMonitor() throws Exception {

        String monitorName = "HTTPSMonitor";
        String jsonString =
                "{\"inet_address\":\"192.178.24.132\",\"response_code\":\"200-201\",\"url\":\"https://www.google.com/\",\"ports\":{\"port\":[433]}}";

        ServiceMonitorManager realManager = mock(ServiceMonitorManager.class);
        when(monitorRegistry.getService(monitorName)).thenReturn(realManager);
        ServiceMonitor monitor = mock(ServiceMonitor.class);
        when(realManager.create()).thenReturn(monitor);
        Message.Builder request = HttpMonitorRequest.newBuilder();
        when(realManager.createRequestBuilder()).thenReturn(request);
        ServiceMonitorResponseImpl response = mock(ServiceMonitorResponseImpl.class);
        when(response.getStatus()).thenReturn(ServiceMonitorResponse.Status.Up);
        when(monitor.poll(any())).thenReturn(future);
        when(future.get(anyLong(), any())).thenReturn(response);

        try {
            FieldUtils.writeField(monitorCommand, "monitorName", monitorName, true);
            FieldUtils.writeField(monitorCommand, "request", jsonString, true);
            FieldUtils.writeField(monitorCommand, "monitorRegistry", monitorRegistry, true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        Object result = monitorCommand.execute();
        Assertions.assertEquals(ServiceMonitorResponse.Status.Up, result);
    }

    @Test
    void testSshMonitor() throws Exception {

        String monitorName = "HTTPSMonitor";
        String jsonString = "{\"timeout\":300,\"retry\":2,\"address\":\"127.0.0.1\",\"port\":22,\"banner\":\"*\"}";

        ServiceMonitorManager realManager = mock(ServiceMonitorManager.class);
        when(monitorRegistry.getService(monitorName)).thenReturn(realManager);
        ServiceMonitor monitor = mock(ServiceMonitor.class);
        when(realManager.create()).thenReturn(monitor);
        Message.Builder request = SshMonitorRequest.newBuilder();
        when(realManager.createRequestBuilder()).thenReturn(request);
        ServiceMonitorResponseImpl response = mock(ServiceMonitorResponseImpl.class);
        when(response.getStatus()).thenReturn(ServiceMonitorResponse.Status.Up);
        when(monitor.poll(any())).thenReturn(future);
        when(future.get(anyLong(), any())).thenReturn(response);

        try {
            FieldUtils.writeField(monitorCommand, "monitorName", monitorName, true);
            FieldUtils.writeField(monitorCommand, "request", jsonString, true);
            FieldUtils.writeField(monitorCommand, "monitorRegistry", monitorRegistry, true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        Object result = monitorCommand.execute();
        Assertions.assertEquals(ServiceMonitorResponse.Status.Up, result);
    }
}

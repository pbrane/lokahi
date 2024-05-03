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
package org.opennms.horizon.minion.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.protobuf.Any;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opennms.horizon.minion.plugin.api.MonitoredService;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorResponse;
import org.opennms.monitors.http.contract.HttpMonitorRequest;
import org.opennms.monitors.http.contract.Port;

public class HttpsMonitorTest {

    @Mock
    MonitoredService monitoredService;

    HttpMonitorRequest httpTestRequest;
    Any testConfig;

    HttpsMonitor httpsMonitor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testHttpsMonitorUpStatus() throws Exception {
        httpsMonitor = new HttpsMonitor();
        testConfig = prepareHttpsMonitorParams(
                "www.google.com", "200-201", "https://www.google.com/", Arrays.asList(443), 2, 3000);
        CompletableFuture<ServiceMonitorResponse> response = httpsMonitor.poll(monitoredService, testConfig);
        ServiceMonitorResponse serviceMonitorResponse = response.get();
        assertEquals(ServiceMonitorResponse.Status.Up, serviceMonitorResponse.getStatus());
        assertTrue(serviceMonitorResponse.getResponseTime() > 0.0);
    }

    @Test
    public void testHttpsMonitorDownStatus() throws Exception {
        httpsMonitor = new HttpsMonitor();
        testConfig = prepareHttpsMonitorParams(
                "www.opennms.org", "100-200", "https://www.opennms.org/", Arrays.asList(443), 3, 3000);
        CompletableFuture<ServiceMonitorResponse> response = httpsMonitor.poll(monitoredService, testConfig);
        ServiceMonitorResponse serviceMonitorResponse = response.get();
        assertEquals(ServiceMonitorResponse.Status.Down, serviceMonitorResponse.getStatus());
        assertEquals(0.0d, serviceMonitorResponse.getResponseTime(), 0);
    }

    Any prepareHttpsMonitorParams(
            final String hostName,
            final String responseCode,
            final String testURl,
            final List<Integer> ports,
            int retry,
            long timeout) {
        httpTestRequest = HttpMonitorRequest.newBuilder()
                .setHostName(hostName)
                .setResponseCode(responseCode)
                .setUrl(testURl)
                .setPorts(Port.newBuilder().addAllPort(ports))
                .setRetry(retry)
                .setTimeout(timeout)
                .build();
        testConfig = Any.pack(httpTestRequest);
        return testConfig;
    }
}

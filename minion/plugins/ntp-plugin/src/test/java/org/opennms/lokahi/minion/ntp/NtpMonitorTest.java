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
package org.opennms.lokahi.minion.ntp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.protobuf.Any;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorResponse;
import org.opennms.monitors.ntp.contract.NTPMonitorRequest;

public class NtpMonitorTest {

    private static final String DEFAULT_TEST_INVALID_IP_ADDRESS_VALUE = "time14.google.com";

    private static final String DEFAULT_TEST_IP_ADDRESS_VALUE = "time.google.com";

    private static final int DEFAULT_TEST_PORT_VALUE = 123;

    private static final int DEFAULT_TEST_RETRIES_VALUE = 3;

    private static final int DEFAULT_TEST_TIMEOUT_VALUE = 5000;

    private static final int NON_DEFAULT_TEST_PORT_VALUE = 124;

    NTPMonitorRequest ntpMonitorRequest;

    Any testConfig;
    NtpMonitor ntpMonitor;

    @Before
    public void setUp() {
        ntpMonitor = new NtpMonitor();

        MockitoAnnotations.openMocks(this);
        ntpMonitorRequest = NTPMonitorRequest.newBuilder()
                .setInetAddress(DEFAULT_TEST_IP_ADDRESS_VALUE)
                .build();

        testConfig = Any.pack(ntpMonitorRequest);
    }

    @Test
    public void pollSuccess() throws Exception {
        testConfig = Any.pack(getNtpMonitorRequest(
                DEFAULT_TEST_IP_ADDRESS_VALUE,
                DEFAULT_TEST_PORT_VALUE,
                DEFAULT_TEST_RETRIES_VALUE,
                DEFAULT_TEST_TIMEOUT_VALUE));
        CompletableFuture<ServiceMonitorResponse> response = ntpMonitor.poll(testConfig);
        ServiceMonitorResponse serviceMonitorResponse = response.get();
        assertEquals(ServiceMonitorResponse.Status.Up, serviceMonitorResponse.getStatus());
        assertTrue(serviceMonitorResponse.getResponseTime() > 0.0);
    }

    @Test
    public void pollUnknownHostException() throws Exception {
        testConfig = Any.pack(getNtpMonitorRequest(
                DEFAULT_TEST_INVALID_IP_ADDRESS_VALUE,
                DEFAULT_TEST_PORT_VALUE,
                DEFAULT_TEST_RETRIES_VALUE,
                DEFAULT_TEST_TIMEOUT_VALUE));
        CompletableFuture<ServiceMonitorResponse> response = ntpMonitor.poll(testConfig);
        ServiceMonitorResponse serviceMonitorResponse = response.get();
        assertEquals(ServiceMonitorResponse.Status.Down, serviceMonitorResponse.getStatus());
        assertTrue(serviceMonitorResponse.getResponseTime() <= 0.0);
    }

    @Test
    public void pollTimeOutException() throws Exception {
        testConfig = Any.pack(getNtpMonitorRequest(
                DEFAULT_TEST_IP_ADDRESS_VALUE,
                NON_DEFAULT_TEST_PORT_VALUE,
                DEFAULT_TEST_RETRIES_VALUE,
                DEFAULT_TEST_TIMEOUT_VALUE));
        CompletableFuture<ServiceMonitorResponse> response = ntpMonitor.poll(testConfig);
        ServiceMonitorResponse serviceMonitorResponse = response.get();
        assertEquals(ServiceMonitorResponse.Status.Unknown, serviceMonitorResponse.getStatus());
        assertTrue(serviceMonitorResponse.getResponseTime() <= 0.0);
    }

    public NTPMonitorRequest getNtpMonitorRequest(String ipAddress, int port, int retries, int timeout) {
        return NTPMonitorRequest.newBuilder()
                .setInetAddress(ipAddress)
                .addAllPort(List.of(port))
                .setRetries(retries)
                .setTimeout(timeout)
                .build();
    }
}

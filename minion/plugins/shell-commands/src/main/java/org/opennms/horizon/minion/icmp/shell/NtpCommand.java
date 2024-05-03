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

import com.google.protobuf.Any;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.horizon.minion.plugin.api.registries.MonitorRegistry;
import org.opennms.horizon.shared.icmp.PingConstants;
import org.opennms.monitors.ntp.contract.NTPMonitorRequest;

@Command(scope = "opennms", name = "ntp-monitor", description = "Ntp Monitor ")
@Service
public class NtpCommand implements Action {
    @Reference
    private MonitorRegistry monitorRegistry;

    @Option(name = "-r", aliases = "--retries", description = "Number of retries")
    int retries = PingConstants.DEFAULT_RETRIES;

    @Option(name = "-t", aliases = "--timeout", description = "Timeout in milliseconds")
    int timeout = PingConstants.DEFAULT_TIMEOUT;

    @Argument(
            index = 0,
            name = "ipAddress",
            description = "Ip address to be pinged",
            required = true,
            multiValued = false)
    String ipAddress;

    @Argument(index = 1, name = "port", description = "port to be pinged", required = true, multiValued = false)
    int port;

    @Override
    public Object execute() throws Exception {

        var scannerManager = monitorRegistry.getService("NTPMonitor");
        var ntpMonitor = scannerManager.create();

        Any configuration = Any.pack(NTPMonitorRequest.newBuilder()
                .setInetAddress(ipAddress)
                .addAllPort(Arrays.asList(port))
                .setRetries(retries)
                .setTimeout(timeout)
                .build());

        var future = ntpMonitor.poll(null, configuration);

        while (true) {
            try {
                try {
                    var response = future.get(1, TimeUnit.SECONDS);
                    System.out.printf("Ntp Monitors result : \n  %s ", response.toString());
                } catch (InterruptedException e) {
                    System.out.println("\n\nInterrupted.");
                } catch (ExecutionException e) {
                    System.out.printf("\n\n Ntp Monitors failed with: %s\n", e);
                }
                break;
            } catch (TimeoutException e) {
                // pass
            }
            System.out.print(".");
        }
        return null;
    }
}

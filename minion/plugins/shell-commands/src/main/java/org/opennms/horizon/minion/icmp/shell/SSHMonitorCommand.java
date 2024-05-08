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
import java.util.concurrent.CompletableFuture;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorResponse;
import org.opennms.horizon.minion.plugin.api.registries.MonitorRegistry;
import org.opennms.ssh.contract.SshMonitorRequest;

@Command(scope = "opennms", name = "ssh-monitor", description = "SSH given host and port")
@Service
public class SSHMonitorCommand implements Action {

    private static final int DEFAULT_RETRIES = 5;
    private static final int DEFAULT_TIMEOUT = 800;
    private static final String DEFAULT_CLIENT_BANNER = "";

    @Reference
    MonitorRegistry monitorRegistry;

    @Option(name = "-r", aliases = "--retries", description = "Number of retries")
    int retries = DEFAULT_RETRIES;

    @Option(name = "-t", aliases = "--timeout", description = "Timeout in milliseconds")
    int timeout = DEFAULT_TIMEOUT;

    @Option(name = "-c", aliases = "--client-banner", description = "Banner")
    String clientBanner = DEFAULT_CLIENT_BANNER;

    @Argument(index = 0, name = "address", description = "SSH address", required = true, multiValued = false)
    String address;

    @Argument(index = 1, name = "port", description = "SSH Port", required = true, multiValued = false)
    int port;

    @Argument(index = 2, name = "banner", description = "SSH Banner", required = true, multiValued = false)
    String banner;

    @Override
    public Object execute() throws Exception {
        System.out.println("SSH Command");
        var sshMonitorManager = monitorRegistry.getService("SSHMonitor");
        var sshMonitor = sshMonitorManager.create();
        Any configuration = Any.pack(SshMonitorRequest.newBuilder()
                .setPort(port)
                .setTimeout(timeout)
                .setRetry(retries)
                .setBanner(banner)
                .setAddress(address)
                .setClientBanner(clientBanner)
                .build());

        System.out.printf("\tAddress: %s\n", address);
        System.out.printf("\tPort: %d\n", port);
        System.out.printf("\tRetries: %d\n", retries);
        System.out.printf("\tTimeout: %d\n", timeout);
        System.out.printf("\tBanner: %s\n", banner);
        System.out.printf("\tClient Banner: %s\n", clientBanner);

        CompletableFuture<ServiceMonitorResponse> future = sshMonitor.poll(configuration);
        future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                System.out.println("Error occurred during pollAsync:" + throwable.getMessage());
            } else {
                System.out.println("pollAsync completed successfully." + result.toString());
            }
        });

        return null;
    }
}

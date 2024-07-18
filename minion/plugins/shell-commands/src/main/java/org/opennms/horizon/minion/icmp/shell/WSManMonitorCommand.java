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
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorManager;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorResponse;
import org.opennms.horizon.minion.plugin.api.registries.MonitorRegistry;
import org.opennms.wsman.contract.WsmanConfiguration;
import org.opennms.wsman.contract.WsmanMonitorRequest;

@Command(scope = "opennms", name = "wsman-monitor", description = "WSMan Monitor")
@Service
public class WSManMonitorCommand implements Action {

    // wsman-monitor
    private static final String DEFAULT_HOST = "192.168.35.117";
    private static final int DEFAULT_MAX_ELEMENTS = 9999;
    private static final int DEFAULT_MAX_ENVELOPE_SIZE = 9999;
    private static final int DEFAULT_CONNECTION_TIMEOUT = 5000;
    private static final int DEFAULT_RECEIVE_TIMEOUT = 10000;

    @Reference
    MonitorRegistry monitorRegistry;

    // Sample commands to invoke WSMAN monitor:
    //    wsman-monitor -m 9999 -s 1000 -t 500 -r 500 -host "172.16.8.221" -port "5985" -path "/wsman"
    //    -u "onms" -p '*****' --node-id 1110 --monitor-service-id 2220 -v 0
    //    -ruri "http://schemas.microsoft.com/wbem/wsman/1/wmi/root/cimv2/Win32_LogicalDisk"
    //    -rl "#DeviceID matches '.*C.*'" -rt 1

    @Option(aliases = "--node-id", name = "-nid", description = "Node id")
    Long nodeId;

    @Option(aliases = "--monitor-service-id", name = "-msid", description = "Monitored service id")
    Long monServiceId;

    @Option(aliases = "--host", name = "-host", description = "Target windows machine to monitor, DNS or IP address")
    String host = DEFAULT_HOST;

    @Option(name = "-h", aliases = "--help", description = "Display command usage")
    boolean helpRequested;

    @Option(name = "-m", aliases = "--max-elements", description = "Max Elements")
    int maxElements = DEFAULT_MAX_ELEMENTS;

    @Option(name = "-s", aliases = "--max-envelope-size", description = "Max Envelope Size")
    int maxEnvelopeSize = DEFAULT_MAX_ENVELOPE_SIZE;

    @Option(name = "-t", aliases = "--connection-timeout", description = "Connection Timeout")
    int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;

    @Option(name = "-r", aliases = "--receive-timeout", description = "Receive Timeout")
    int receiveTimeout = DEFAULT_RECEIVE_TIMEOUT;

    @Option(aliases = "--port", name = "-port", description = "Target port")
    int port;

    @Option(aliases = "--retry-count", name = "-rt", description = "Number of retries to contact node")
    int retry;

    @Option(aliases = "--url-path", name = "-path", description = "URL path, i.e, /wsman")
    String path;

    @Option(aliases = "--user-name", name = "-u", description = "Username")
    String username;

    @Option(aliases = "--password", name = "-p", description = "Password")
    String password;

    @Option(name = "-gss", aliases = "--gss-auth", description = "GSS Authentication, default is false")
    boolean gssAuth = false;

    @Option(name = "-ssl", aliases = "--strict-ssl", description = "Strict SSL, default is false")
    boolean strictSSL = false;

    @Option(name = "-v", aliases = "--server-version", description = "Server Version")
    int serverVersion;

    @Option(aliases = "--resource-uri", name = "-ruri", description = "Resource URI")
    String resourceURI;

    @Option(aliases = "--rule", name = "-rl", description = "Rule to apply on selectors")
    String rule;

    @Override
    public Object execute() throws Exception {
        System.out.println("Executing WS_MAN Command....! \n");

        Map<String, ServiceMonitorManager> services = monitorRegistry.getServices();
        var wsmanMonitorManager = monitorRegistry.getService("WSMANMonitor");

        if (wsmanMonitorManager == null) {
            System.out.println("Unable to process command as monitor service not registered. ");
            return null;
        }

        var wsmanMonitor = wsmanMonitorManager.create();

        Any configuration = Any.pack(WsmanMonitorRequest.newBuilder()
                .setAgentConfiguration(WsmanConfiguration.newBuilder()
                        .setHost(host)
                        .setUsername(username)
                        .setPassword(password)
                        .setGssAuth(gssAuth)
                        .setStrictSsl(strictSSL)
                        .setMaxElements(maxElements)
                        .setMaxEnvelopeSize(maxEnvelopeSize)
                        .setConnectionTimeout(connectionTimeout)
                        .setReceiveTimeout(receiveTimeout)
                        .setPort(port)
                        .setPath(path)
                        .setRetries(retry)
                        .setResourceUri(resourceURI)
                        .build())
                .setRule(rule)
                .setServerVersionValue(serverVersion)
                // .setServiceInventory(ServiceInventory.newBuilder()
                //  .setNodeId(nodeId)
                // .setMonitorServiceId(monServiceId)
                //   .build())
                .addSelectors(org.opennms.wsman.contract.Selector.newBuilder()
                        .setKey(String.valueOf("selector.DeviceID"))
                        .setValue(String.valueOf("C:"))
                        .build())
                .build());

        try {
            WsmanMonitorRequest wsmanRequest = configuration.unpack(WsmanMonitorRequest.class);

        } catch (Exception e) {
            System.err.println("Error unpacking configuration: " + e.getMessage());
        }

        System.out.println("Calling WSMAN Monitor Polling ....\n");

        CompletableFuture<ServiceMonitorResponse> future = wsmanMonitor.poll(configuration);

        future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                System.out.println("Error occurred during call:" + throwable.getMessage());
                throwable.printStackTrace();
            } else {
                System.out.println("Call completed successfully." + result.toString());
            }
        });

        return null;
    }
}

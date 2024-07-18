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
import org.opennms.horizon.minion.plugin.api.ServiceDetectorManager;
import org.opennms.horizon.minion.plugin.api.registries.DetectorRegistry;
import org.opennms.node.scan.contract.ServiceResult;
import org.opennms.wsman.contract.WsmanConfiguration;
import org.opennms.wsman.contract.WsmanDetectorRequest;

@Command(scope = "opennms", name = "wsman-detector", description = "WSMan Detector")
@Service
public class WSManDetectorCommand implements Action {

    private static final String DEFAULT_HOST = "192.168.35.117";
    private static final int DEFAULT_MAX_ELEMENTS = 9999;
    private static final int DEFAULT_MAX_ENVELOPE_SIZE = 9999;
    private static final int DEFAULT_CONNECTION_TIMEOUT = 50000;
    private static final int DEFAULT_RECEIVE_TIMEOUT = 10000;

    @Reference
    DetectorRegistry detectorRegistry;

    // Sample commands to invoke WSMAN detector:
    // wsman-detector -m 9999 -s 1000 --connection-timeout 50000 -r 50000 -host "172.16.8.221" -port "5985"
    // -path "/wsman" -u "onms" -p '******' --node-id 1110 --monitor-service-id 2220 -v 0 -rt 1

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

    @Override
    public Object execute() throws Exception {
        System.out.println(">>> Executing WS_MAN Command....!");

        Map<String, ServiceDetectorManager> services = detectorRegistry.getServices();
        var wsmanDetectorManager = detectorRegistry.getService("WSMANDetector");

        if (wsmanDetectorManager == null) {
            System.out.println(">>> Unable to process command as detector service not registered. ");
            return null;
        }

        var wsmanDetector = wsmanDetectorManager.create();

        Any configuration = Any.pack(WsmanDetectorRequest.newBuilder()
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
                        .build())
                .setServerVersionValue(serverVersion)
                /*                .setServiceInventory(ServiceInventory.newBuilder()
                .setNodeId(nodeId)
                .setMonitorServiceId(monServiceId)
                .build())*/
                .build());

        try {
            WsmanDetectorRequest wsmanRequest = configuration.unpack(WsmanDetectorRequest.class);

        } catch (Exception e) {
            System.err.println("Error unpacking configuration: " + e.getMessage());
        }

        CompletableFuture<ServiceResult> future = wsmanDetector.detect(host, configuration);

        future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                System.out.println("\n\n>> Error occurred during call:" + throwable.getMessage());
                throwable.printStackTrace();
            } else {
                System.out.println("\n\n>> Detector completed with result:");
                System.out.println("===================================");
                System.out.println(">> Detected? " + result.getStatus());
                System.out.println(">> Host: " + result.getIpAddress());
                System.out.println(">> Service: " + result.getService());
                System.out.println("===================================");
            }
        });

        return null;
    }
}

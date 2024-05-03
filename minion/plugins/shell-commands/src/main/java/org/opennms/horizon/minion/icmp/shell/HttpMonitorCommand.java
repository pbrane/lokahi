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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.horizon.minion.plugin.api.HttpConstant;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorResponse;
import org.opennms.horizon.minion.plugin.api.registries.MonitorRegistry;
import org.opennms.monitors.http.contract.AuthParams;
import org.opennms.monitors.http.contract.BasicAuthParams;
import org.opennms.monitors.http.contract.HttpMonitorRequest;
import org.opennms.monitors.http.contract.Port;

@Command(scope = "opennms", name = "http-monitor", description = "Monitor http services.")
@Service
public class HttpMonitorCommand implements Action {

    @Reference
    MonitorRegistry monitorRegistry;

    @Argument(
            index = 0,
            name = "Expected responses",
            description = "Http response codes.",
            required = true,
            multiValued = false)
    String responseCodes;

    @Argument(index = 1, name = "url", description = "Service url.", required = true, multiValued = false)
    String url;

    @Argument(index = 2, name = "ports", description = "List of Ports", required = true, multiValued = true)
    List<Integer> ports;

    @Option(name = "-a", aliases = "--ipaddress", description = "IP Address")
    String ipAddress = "";

    @Option(name = "-h", aliases = "--hostname", description = "Host Name")
    String hostName = "";

    @Option(name = "-r", aliases = "--retries", description = "Number of retries")
    int retries = HttpConstant.DEFAULT_RETRIES;

    @Option(name = "-t", aliases = "--timeout", description = "Timeout in milliseconds")
    int timeout = HttpConstant.DEFAULT_TIMEOUT;

    @Option(name = "-u", aliases = "--username", description = "User name")
    String userName = "";

    @Option(name = "-p", aliases = "--password", description = "Password")
    String password = "";

    @Option(name = "-rt", aliases = "--response-text", description = "Response Text")
    String responseText = "";

    @Option(name = "-ua", aliases = "--user-agent", description = "User Agent")
    String userAgent = "";

    @Override
    public Object execute() {

        System.out.printf("IP Address %s :\n", ipAddress);
        System.out.printf("HostName %s :\n", hostName);
        System.out.printf("\tResponse Code: %s\n", responseCodes);
        System.out.printf("\tTimeout: %d\n", timeout);
        System.out.printf("\tRetries: %d\n", retries);
        System.out.printf("\tURL: %s\n", url);
        System.out.printf("\tUserName: %s\n", userName);
        System.out.printf("\tPassword: %s\n", password);
        System.out.printf("\tResponse Text: %s\n", responseText);
        System.out.printf("\tUser Agent: %s\n", userAgent);

        BasicAuthParams basicAuthParams = BasicAuthParams.newBuilder()
                .setUserName(userName)
                .setPassword(password)
                .build();

        AuthParams authParams =
                AuthParams.newBuilder().setBasicAuthParams(basicAuthParams).build();

        var httpMonitorManager = monitorRegistry.getService("HTTPMonitor");
        var httpMonitor = httpMonitorManager.create();
        Any configuration = Any.pack(HttpMonitorRequest.newBuilder()
                .setInetAddress(ipAddress)
                .setHostName(hostName)
                .setResponseCode(responseCodes)
                .setUrl(url)
                .setRetry(retries)
                .setTimeout(timeout)
                .setUserAgent(userAgent)
                .setResponseText(responseText)
                .setAuthParams(authParams)
                .setPorts(Port.newBuilder().addAllPort(ports).build())
                .build());

        CompletableFuture<ServiceMonitorResponse> response = httpMonitor.poll(null, configuration);
        try {
            ServiceMonitorResponse serviceMonitorResponse = response.get(1, TimeUnit.SECONDS);
            System.out.println("HttpMonitor status: " + serviceMonitorResponse.getStatus());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }

        return "Processed";
    }
}

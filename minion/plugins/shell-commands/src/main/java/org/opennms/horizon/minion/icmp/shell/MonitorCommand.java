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

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
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

@Command(scope = "opennms", name = "-scan", description = "Monitor Scan ")
@Service
public class MonitorCommand implements Action {

    @Reference
    private MonitorRegistry monitorRegistry;

    @Option(name = "-req", aliases = "--request", description = "JSON string containing monitor execution request data")
    private String request;

    @Argument(
            index = 0,
            name = "monitor name",
            description = "monitor to be pinged",
            required = true,
            multiValued = false)
    private String monitorName;

    @Override
    public Object execute() {
        final var manager = this.monitorRegistry.getService(this.monitorName);
        if (manager == null) {
            System.err.println("No such monitor: " + this.monitorName);
            return null;
        }

        final Any configuration;
        try {
            final var builder = manager.createRequestBuilder();
            JsonFormat.parser().merge(this.request, builder);

            if (!builder.isInitialized()) {
                System.err.println("Error initializing request: " + builder.getInitializationErrorString());
                return null;
            }

            configuration = Any.pack(builder.build());

            if (configuration.getValue() != null) {
                System.out.println(request);
            }

        } catch (final InvalidProtocolBufferException e) {
            System.err.println("Error parsing request: " + e.getMessage());
            return null;
        }

        final var monitor = manager.create();

        var future = monitor.poll(configuration);

        while (true) {
            try {
                final var response = future.get(1, TimeUnit.SECONDS);
                System.out.println("Done");
                System.out.printf("%s result:\n%s\n", this.monitorName, response.toString());
                return response.getStatus();
            } catch (ExecutionException e) {
                System.out.println("Failed");
                System.err.printf("%s failed with:\n%s\n", this.monitorName, e);
                return null;
            } catch (InterruptedException e) {
                return null;
            } catch (TimeoutException e) {
                System.out.print(".");
            }
        }
    }
}

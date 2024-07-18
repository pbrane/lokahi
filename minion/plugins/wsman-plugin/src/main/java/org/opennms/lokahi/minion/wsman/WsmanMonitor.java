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
package org.opennms.lokahi.minion.wsman;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.protobuf.Any;
import com.google.protobuf.Descriptors;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.opennms.core.wsman.WSManClient;
import org.opennms.core.wsman.WSManClientFactory;
import org.opennms.core.wsman.WSManEndpoint;
import org.opennms.core.wsman.cxf.CXFWSManClientFactory;
import org.opennms.core.wsman.exceptions.WSManException;
import org.opennms.core.wsman.utils.ResponseHandlingUtils;
import org.opennms.core.wsman.utils.RetryNTimesLoop;
import org.opennms.core.wsman.utils.WsmanEndpointUtils;
import org.opennms.horizon.minion.plugin.api.PollStatus;
import org.opennms.horizon.minion.plugin.api.ServiceMonitor;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorResponse;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorResponseImpl;
import org.opennms.wsman.contract.WsmanConfiguration;
import org.opennms.wsman.contract.WsmanMonitorRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

public class WsmanMonitor implements ServiceMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(WsmanMonitor.class);
    private Descriptors.FieldDescriptor resourceUriFieldDescriptor;
    private Descriptors.FieldDescriptor ruleFieldDescriptor;
    private WSManClientFactory m_factory = new CXFWSManClientFactory();

    public WsmanMonitor() {}

    @Override
    public CompletableFuture<ServiceMonitorResponse> poll(Any config) {

        LOG.debug("POLL - WSMAN monitor poll has been invoked with config: " + config);
        WsmanMonitorRequest wsmanMonitorRequest = null;
        CompletableFuture<ServiceMonitorResponse> future = new CompletableFuture<>();
        try {

            if (!config.is(WsmanMonitorRequest.class)) {
                throw new IllegalArgumentException(
                        "configuration must be an WsmanMonitorRequest; type-url=" + config.getTypeUrl());
            }

            wsmanMonitorRequest = config.unpack(WsmanMonitorRequest.class);

            Descriptors.Descriptor wsmanMonitorRequestDescriptor =
                    WsmanMonitorRequest.getDefaultInstance().getDescriptorForType();
            Descriptors.FieldDescriptor wsmanConfigurationFieldDescriptor =
                    wsmanMonitorRequestDescriptor.findFieldByNumber(
                            WsmanMonitorRequest.AGENTCONFIGURATION_FIELD_NUMBER);
            WsmanConfiguration wsmanConfiguration = wsmanMonitorRequest.getAgentConfiguration();

            if (!wsmanMonitorRequest.hasField(wsmanConfigurationFieldDescriptor)) {
                throw new IllegalArgumentException("Error: Wsman Agent COnfigurations are missing.");
            }

            Descriptors.Descriptor wsmanConfigurationDescriptor = wsmanConfiguration.getDescriptorForType();
            resourceUriFieldDescriptor =
                    wsmanConfigurationDescriptor.findFieldByNumber(WsmanConfiguration.RESOURCE_URI_FIELD_NUMBER);
            ruleFieldDescriptor =
                    wsmanMonitorRequestDescriptor.findFieldByNumber(WsmanMonitorRequest.RULE_FIELD_NUMBER);

            if (!wsmanConfiguration.hasField(resourceUriFieldDescriptor)) {
                throw new IllegalArgumentException(
                        "'" + resourceUriFieldDescriptor.getFullName() + "' parameter is required.");
            }
            if (!wsmanMonitorRequest.hasField(ruleFieldDescriptor)) {
                throw new IllegalArgumentException(
                        "'" + ruleFieldDescriptor.getFullName() + "' parameter is required.");
            }

            // Now you can access and print all the fields of the wsmanRequest object
            LOG.debug("\n\n-----------New WSMAN Request-------------");
            LOG.debug("Host: {}", wsmanConfiguration.getHost());
            LOG.debug("Port: {}", wsmanConfiguration.getPort());
            LOG.debug("Path: {}", wsmanConfiguration.getPath());
            LOG.debug("Strict SSL: {}", wsmanConfiguration.getStrictSsl());
            LOG.debug("Server Version: {}", wsmanMonitorRequest.getServerVersionValue());
            LOG.debug("Max Elements: {}", wsmanConfiguration.getMaxElements());
            LOG.debug("Max Envelope Size: {}", wsmanConfiguration.getMaxEnvelopeSize());
            LOG.debug("Connection Timeout: {}", wsmanConfiguration.getConnectionTimeout());
            LOG.debug("Receive Timeout: {}", wsmanConfiguration.getReceiveTimeout());
            /*            LOG.debug("ServiceInventory: {}", wsmanMonitorRequest.getServiceInventory());
            LOG.debug("Node-Id: {}", wsmanMonitorRequest.getServiceInventory().getNodeId());
            LOG.debug(
                    "Monitor-Service-Id: {}",
                    wsmanMonitorRequest.getServiceInventory().getMonitorServiceId());*/

            InetAddress iNet = InetAddress.getByName(wsmanConfiguration.getHost());

            final String ipAddress = iNet.getHostAddress();

            future = pollAsync(wsmanMonitorRequest, LOG)
                    .thenApply(pollStatus -> {
                        ServiceMonitorResponse serviceMonitorResponse = ServiceMonitorResponseImpl.builder()
                                .reason(pollStatus.getReason())
                                .status(
                                        pollStatus.getStatusName().equalsIgnoreCase("up")
                                                ? ServiceMonitorResponse.Status.Up
                                                : ServiceMonitorResponse.Status.Down)
                                .build();
                        return serviceMonitorResponse;
                    })
                    .completeOnTimeout(ServiceMonitorResponseImpl.builder().build(), 3000, TimeUnit.MILLISECONDS)
                    .exceptionally(thrown -> ServiceMonitorResponseImpl.builder()
                            .status(ServiceMonitorResponse.Status.Unknown)
                            .reason(thrown.getMessage())
                            .build());

            LOG.debug("POLL - WsManMonitor returned normally.");
            return future;

        } catch (Exception e) {
            LOG.debug(
                    "Unexpected exception during WSMAN poll {}, {}, {}",
                    wsmanMonitorRequest.getAgentConfiguration().getHost(),
                    wsmanMonitorRequest.getAgentConfiguration().getPort(),
                    e);
            future.completeExceptionally(e);
        }

        return future;
    }

    public CompletableFuture<PollStatus> pollAsync(WsmanMonitorRequest wsmanMonitorRequest, Logger LOG) {

        return CompletableFuture.supplyAsync(() -> {
            PollStatus ps = PollStatus.down();

            try {

                WSManClientFactory m_factory = new CXFWSManClientFactory();
                WsmanConfiguration wsmanConfiguration = wsmanMonitorRequest.getAgentConfiguration();
                InetAddress address = InetAddress.getByName(wsmanConfiguration.getHost());

                String rule = wsmanMonitorRequest.getRule();
                String resourceUri = wsmanConfiguration.getResourceUri();

                LOG.debug("WSMAN - ResourceUri, rule: {}, {}", resourceUri, rule);
                // convert selectors proto list to hash map, as we need to pass on to wsman client:
                final Map<String, String> selectors = Maps.newHashMap();
                for (org.opennms.wsman.contract.Selector selector : wsmanMonitorRequest.getSelectorsList()) {
                    selectors.put(selector.getKey().replaceFirst("selector.", ""), selector.getValue());
                }
                LOG.debug("WSMAN - selectors: " + selectors);

                WSManEndpoint endpoint;
                endpoint = WsmanEndpointUtils.fromWsmanMonitorRequest(address, wsmanMonitorRequest);
                LOG.debug("WSMAN - endpoint formed: " + endpoint);

                WSManClient client = m_factory.getClient(endpoint);
                int retries = wsmanConfiguration.getRetries();

                LOG.debug("We will retry for {} time(s)", retries);
                RetryNTimesLoop retryLoop = new RetryNTimesLoop(retries);

                Node node = null;
                try {
                    while (retryLoop.shouldContinue()) {
                        try {
                            node = client.get(resourceUri, selectors);
                            break;
                        } catch (WSManException e) {
                            retryLoop.takeException(e);
                        }
                    }
                } catch (WSManException e) {
                    LOG.debug("WSMAN - NODE DOWN: {}, {} ", wsmanConfiguration.getHost(), e.toString());
                    ps.setReason("WSManException: " + e.toString());
                }

                if (node == null) {
                    LOG.debug("WSMAN - DOWN: {}, {} ", wsmanConfiguration.getHost(), "Node is null");
                    ps.setReason("Node is null");
                }

                // Verify the results
                final ListMultimap<String, String> elementValues = ResponseHandlingUtils.toMultiMap(node);
                try {
                    ResponseHandlingUtils.getMatchingIndex(rule, elementValues);
                    // Hey, We've successfully matched an index
                    LOG.debug(
                            "WSMAN - Status UP, Congrats, Selector(s) has matched: {}, {} ",
                            wsmanConfiguration.getHost(),
                            "Node is UP");
                    ps = PollStatus.up();
                    ps.setReason("Node found and responded!");
                } catch (Exception e) {
                    LOG.debug("WSMAN - Node DOWN: {}, {} ", wsmanConfiguration.getHost(), e);
                    ps.setReason("Exception: " + e.toString());
                }

            } catch (Exception e) {
                LOG.debug(
                        "WSMAN - Node DOWN Hay: {}, {} ",
                        wsmanMonitorRequest.getAgentConfiguration().getHost(),
                        e);
                ps.setReason("Exception: " + e.toString());
            }

            return ps;
        });
    }
}

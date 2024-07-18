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
package org.opennms.horizon.minion.azure;

import com.google.protobuf.Any;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.opennms.azure.contract.AzureCollectorRequest;
import org.opennms.azure.contract.AzureCollectorResourcesRequest;
import org.opennms.horizon.azure.api.AzureResponseMetric;
import org.opennms.horizon.azure.api.AzureResultMetric;
import org.opennms.horizon.azure.api.AzureValueMetric;
import org.opennms.horizon.azure.api.AzureValueType;
import org.opennms.horizon.minion.plugin.api.CollectionRequest;
import org.opennms.horizon.minion.plugin.api.CollectionSet;
import org.opennms.horizon.minion.plugin.api.ServiceCollector;
import org.opennms.horizon.minion.plugin.api.ServiceCollectorResponseImpl;
import org.opennms.horizon.shared.azure.http.AzureHttpClient;
import org.opennms.horizon.shared.azure.http.AzureHttpException;
import org.opennms.horizon.shared.azure.http.dto.instanceview.AzureInstanceView;
import org.opennms.horizon.shared.azure.http.dto.login.AzureOAuthToken;
import org.opennms.horizon.shared.azure.http.dto.metrics.AzureMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AzureCollector implements ServiceCollector {
    private final Logger log = LoggerFactory.getLogger(AzureCollector.class);

    private static final String INTERVAL_PARAM = "interval";
    private static final String METRIC_NAMES_PARAM = "metricnames";

    private static final String TIMESPAN_PARAM = "timespan";

    // Reference inventory: TaskUtils.DEFAULT_SCHEDULE
    private static final int TIMESPAN_SECOND = 60;

    // Supported intervals: PT1M,PT5M,PT15M,PT30M,PT1H,PT6H,PT12H,P1D
    private static final String METRIC_INTERVAL = "PT1M";

    // Azure Metric Key - Metrics Processor Key
    private static final Map<String, String> AZURE_NODE_METRIC_TO_ALIAS = Map.of(
            "Network In Total", "network_in_total_bytes",
            "Network Out Total", "network_out_total_bytes");

    // Valid metrics: BytesSentRate,BytesReceivedRate,PacketsSentRate,PacketsReceivedRate
    // https://learn.microsoft.com/en-us/azure/azure-monitor/reference/supported-metrics/microsoft-network-networkinterfaces-metrics
    private static final Map<String, String> AZURE_INTERFACE_METRIC_TO_ALIAS = Map.of(
            "BytesReceivedRate", "bytes_received_rate",
            "BytesSentRate", "bytes_sent_rate");

    // Valid metrics:
    // PacketsInDDoS,PacketsDroppedDDoS,PacketsForwardedDDoS,TCPPacketsInDDoS,TCPPacketsDroppedDDoS,TCPPacketsForwardedDDoS,UDPPacketsInDDoS,UDPPacketsDroppedDDoS,UDPPacketsForwardedDDoS,BytesInDDoS,BytesDroppedDDoS,BytesForwardedDDoS,TCPBytesInDDoS,TCPBytesDroppedDDoS,TCPBytesForwardedDDoS,UDPBytesInDDoS,UDPBytesDroppedDDoS,UDPBytesForwardedDDoS,IfUnderDDoSAttack,DDoSTriggerTCPPackets,DDoSTriggerUDPPackets,DDoSTriggerSYNPackets,VipAvailability,ByteCount,PacketCount,SynCount
    // https://learn.microsoft.com/en-us/azure/azure-monitor/reference/supported-metrics/microsoft-network-publicipaddresses-metrics
    private static final Map<String, String> AZURE_IPINTERFACE_METRIC_TO_ALIAS = Map.of("ByteCount", "bytes_received");

    private static final String AZURE_NODE_PREFIX = "azure-node-";
    private static final String METRIC_DELIMITER = ",";

    private final AzureHttpClient client;

    public AzureCollector(AzureHttpClient client) {
        this.client = client;
    }

    @Override
    public CompletableFuture<CollectionSet> collect(CollectionRequest collectionRequest, Any config) {
        CompletableFuture<CollectionSet> future = new CompletableFuture<>();

        try {
            if (!config.is(AzureCollectorRequest.class)) {
                throw new IllegalArgumentException(
                        "configuration must be an AzureCollectorRequest; type-url=" + config.getTypeUrl());
            }

            AzureCollectorRequest request = config.unpack(AzureCollectorRequest.class);

            AzureOAuthToken token = client.login(
                    request.getDirectoryId(),
                    request.getClientId(),
                    request.getClientSecret(),
                    request.getTimeoutMs(),
                    request.getRetries());

            AzureInstanceView instanceView = client.getInstanceView(
                    token,
                    request.getSubscriptionId(),
                    request.getResourceGroup(),
                    request.getResource(),
                    request.getTimeoutMs(),
                    request.getRetries());

            if (instanceView.isUp() && instanceView.isReady()) {

                // host metrics
                List<AzureResultMetric> metricResults = collectNodeMetrics(request, token).entrySet().stream()
                        .map(nodeMetric -> mapNodeResult(request, nodeMetric))
                        .collect(Collectors.toCollection(ArrayList::new));

                // interface metrics
                collectNetworkMetrics(request, token, metricResults);

                log.debug("AZURE COLLECTOR metricResults LIST: {}", metricResults);

                AzureResponseMetric results = AzureResponseMetric.newBuilder()
                        .addAllResults(metricResults)
                        .build();

                future.complete(ServiceCollectorResponseImpl.builder()
                        .results(results)
                        .nodeId(collectionRequest.getNodeId())
                        .status(true)
                        .timeStamp(System.currentTimeMillis())
                        .ipAddress(AZURE_NODE_PREFIX + collectionRequest.getNodeId())
                        .build());

            } else {
                future.complete(ServiceCollectorResponseImpl.builder()
                        .nodeId(collectionRequest.getNodeId())
                        .status(false)
                        .timeStamp(System.currentTimeMillis())
                        .ipAddress(AZURE_NODE_PREFIX + collectionRequest.getNodeId())
                        .build());
            }
        } catch (Exception e) {
            log.error(
                    "Failed to collect for azure resource nodeId: {}, error: {}",
                    collectionRequest.getNodeId(),
                    e.getMessage(),
                    e);
            future.complete(ServiceCollectorResponseImpl.builder()
                    .nodeId(collectionRequest.getNodeId())
                    .status(false)
                    .timeStamp(System.currentTimeMillis())
                    .ipAddress(AZURE_NODE_PREFIX + collectionRequest.getNodeId())
                    .build());
        }
        return future;
    }

    private Map<String, Double> collectNodeMetrics(AzureCollectorRequest request, AzureOAuthToken token)
            throws AzureHttpException {
        Map<String, String> params = getMetricsParams(AZURE_NODE_METRIC_TO_ALIAS.keySet());

        AzureMetrics metrics = client.getMetrics(
                token,
                request.getSubscriptionId(),
                request.getResourceGroup(),
                request.getResource(),
                params,
                request.getTimeoutMs(),
                request.getRetries());

        return metrics.collect();
    }

    private void collectNetworkMetrics(
            AzureCollectorRequest request, AzureOAuthToken token, List<AzureResultMetric> metricResults) {
        for (var resource : request.getCollectorResourcesList()) {
            try {
                metricResults.addAll(collectNetworkMetric(request, resource, token).entrySet().stream()
                        .map(interfaceMetric -> mapInterfaceResult(
                                request, resource.getResource(), resource.getType(), interfaceMetric))
                        .toList());
            } catch (AzureHttpException ex) {
                log.warn(
                        "Skip failed network collection. resource: {}, type: {}, error: {}",
                        resource.getResource(),
                        resource.getType(),
                        ex.getMessage());
            }
        }
    }

    private Map<String, Double> collectNetworkMetric(
            AzureCollectorRequest request, AzureCollectorResourcesRequest resource, AzureOAuthToken token)
            throws AzureHttpException {
        try {
            var type = AzureHttpClient.ResourcesType.fromMetricName(resource.getType());
            Map<String, String> params = getMetricsParams(
                    AzureHttpClient.ResourcesType.NETWORK_INTERFACES == type
                            ? AZURE_INTERFACE_METRIC_TO_ALIAS.keySet()
                            : AZURE_IPINTERFACE_METRIC_TO_ALIAS.keySet());

            AzureMetrics metrics = client.getNetworkInterfaceMetrics(
                    token,
                    request.getSubscriptionId(),
                    request.getResourceGroup(),
                    resource.getType() + "/" + resource.getResource(),
                    params,
                    request.getTimeoutMs(),
                    request.getRetries());

            return metrics.collect();
        } catch (IllegalArgumentException ex) {
            throw new AzureHttpException("Unknown type: " + resource.getType());
        }
    }

    private Map<String, String> getMetricsParams(Collection<String> metricNames) {
        Map<String, String> params = new HashMap<>();
        params.put(INTERVAL_PARAM, METRIC_INTERVAL);
        // limit cost, start time must be at least 1 min before
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(-60L);
        String toTime = now.toString();
        // at least 2 min range
        params.put(TIMESPAN_PARAM, now.plusSeconds(-TIMESPAN_SECOND - 60L).toString() + "/" + toTime);
        params.put(METRIC_NAMES_PARAM, String.join(METRIC_DELIMITER, metricNames));
        return params;
    }

    private AzureResultMetric mapNodeResult(AzureCollectorRequest request, Map.Entry<String, Double> metricData) {
        return AzureResultMetric.newBuilder()
                .setResourceGroup(request.getResourceGroup())
                .setResourceName(request.getResource())
                .setType("node")
                .setAlias(getNodeMetricAlias(metricData.getKey()))
                .setValue(AzureValueMetric.newBuilder()
                        .setType(AzureValueType.INT64)
                        .setUint64(metricData.getValue().longValue())
                        .build())
                .build();
    }

    private AzureResultMetric mapInterfaceResult(
            AzureCollectorRequest request, String resourceName, String type, Map.Entry<String, Double> metricData) {
        return AzureResultMetric.newBuilder()
                .setResourceGroup(request.getResourceGroup())
                .setResourceName(resourceName)
                .setType(type)
                .setAlias(getInterfaceMetricAlias(metricData.getKey()))
                .setValue(AzureValueMetric.newBuilder()
                        .setType(AzureValueType.INT64)
                        .setUint64(metricData.getValue().longValue())
                        .build())
                .build();
    }

    private String getNodeMetricAlias(String metricName) {
        if (AZURE_NODE_METRIC_TO_ALIAS.containsKey(metricName)) {
            return AZURE_NODE_METRIC_TO_ALIAS.get(metricName);
        }
        throw new IllegalArgumentException("Failed to find alias '" + metricName + "' - shouldn't be reached");
    }

    private String getInterfaceMetricAlias(String metricName) {
        if (AZURE_INTERFACE_METRIC_TO_ALIAS.containsKey(metricName)) {
            return AZURE_INTERFACE_METRIC_TO_ALIAS.get(metricName);
        }
        if (AZURE_IPINTERFACE_METRIC_TO_ALIAS.containsKey(metricName)) {
            return AZURE_IPINTERFACE_METRIC_TO_ALIAS.get(metricName);
        }
        throw new IllegalArgumentException("Failed to find alias '" + metricName + "' - shouldn't be reached");
    }
}

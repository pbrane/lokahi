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
package org.opennms.horizon.server.service;

import static java.util.Objects.isNull;
import static org.opennms.horizon.server.service.metrics.Constants.AVG_RESPONSE_TIME;
import static org.opennms.horizon.server.service.metrics.Constants.AZURE_SCAN_TYPE;
import static org.opennms.horizon.server.service.metrics.Constants.INSTANCE_KEY;
import static org.springframework.util.CollectionUtils.isEmpty;

import io.leangen.graphql.execution.ResolutionEnvironment;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.opennms.horizon.inventory.dto.IpInterfaceDTO;
import org.opennms.horizon.inventory.dto.MonitoredServiceQuery;
import org.opennms.horizon.inventory.dto.NodeDTO;
import org.opennms.horizon.server.model.TSResult;
import org.opennms.horizon.server.model.TimeRangeUnit;
import org.opennms.horizon.server.model.TimeSeriesQueryResult;
import org.opennms.horizon.server.model.inventory.MonitoredServiceStatusRequest;
import org.opennms.horizon.server.model.inventory.TopNNode;
import org.opennms.horizon.server.model.status.NodeReachability;
import org.opennms.horizon.server.model.status.NodeResponseTime;
import org.opennms.horizon.server.model.status.NodeStatus;
import org.opennms.horizon.server.service.grpc.InventoryClient;
import org.opennms.horizon.server.service.metrics.Constants;
import org.opennms.horizon.server.service.metrics.GraphQLTSDBMetricsService;
import org.opennms.horizon.server.utils.ServerHeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
@RequiredArgsConstructor
public class NodeStatusService {
    private static final Logger LOG = LoggerFactory.getLogger(NodeStatusService.class);
    private static final String RESPONSE_TIME_METRIC = "response_time_msec";
    private static final int TIME_RANGE_IN_SECONDS = 90;
    private final InventoryClient client;
    private final GraphQLTSDBMetricsService graphQLTSDBMetricsService;
    private final ServerHeaderUtil headerUtil;

    public Mono<NodeStatus> getNodeStatus(long id, String monitorType, ResolutionEnvironment env) {
        NodeDTO node = client.getNodeById(id, headerUtil.getAuthHeader(env));
        return getNodeStatus(node, monitorType, env);
    }

    public Mono<NodeStatus> getNodeStatus(NodeDTO node, String monitorType, ResolutionEnvironment env) {

        if (AZURE_SCAN_TYPE.equals(node.getScanType())) {
            return getStatusMetric("azure-" + node.getId(), env) // TODO: follow up issue: LOK-2653
                    .map(result -> getNodeStatus(node.getId(), result));
        } else {
            if (node.getIpInterfacesCount() > 0) {
                IpInterfaceDTO ipInterface = getPrimaryInterface(node);
                return getNodeStatusByInterface(node.getId(), monitorType, ipInterface, env);
            }
        }
        return Mono.just(new NodeStatus(node.getId(), false));
    }

    private IpInterfaceDTO getPrimaryInterface(NodeDTO node) {
        List<IpInterfaceDTO> ipInterfacesList = node.getIpInterfacesList();
        for (IpInterfaceDTO ipInterface : ipInterfacesList) {
            if (ipInterface.getSnmpPrimary()) {
                return ipInterface;
            }
        }
        return node.getIpInterfaces(0);
    }

    private Mono<NodeStatus> getNodeStatusByInterface(
            long id, String monitorType, IpInterfaceDTO ipInterface, ResolutionEnvironment env) {

        return Mono.fromCallable(() -> client.getMonitoredService(
                        MonitoredServiceQuery.newBuilder()
                                .setMonitoredServiceType(monitorType)
                                .setNodeId(id)
                                .setIpAddress(ipInterface.getIpAddress())
                                .setTenantId(ipInterface.getTenantId())
                                .build(),
                        headerUtil.getAuthHeader(env)))
                .flatMap(monitoredService -> getStatusMetric(monitoredService.getMonitoredEntityId(), env)
                        .map(result -> getNodeStatus(id, result)))
                .onErrorResume(e -> {
                    LOG.error(
                            "Exception while fetching monitored service for node id : {} , ip-address : {}",
                            id,
                            ipInterface.getIpAddress(),
                            e);
                    return Mono.just(new NodeStatus(id, false));
                });
    }

    private NodeStatus getNodeStatus(long id, TimeSeriesQueryResult result) {
        if (isNull(result)) {
            return new NodeStatus(id, false);
        }
        List<TSResult> tsResults = result.getData().getResult();

        if (isEmpty(tsResults)) {
            return new NodeStatus(id, false);
        }

        TSResult tsResult = tsResults.get(0);
        List<List<Double>> values = tsResult.getValues();

        if (isEmpty(values)) {
            return new NodeStatus(id, false);
        }

        List<Double> doubles = values.get(values.size() - 1);
        if (doubles.size() != 2) {
            return new NodeStatus(id, false);
        }

        Double responseTime = doubles.get(1);
        boolean status = responseTime > 0d;

        return new NodeStatus(id, status);
    }

    private Mono<TimeSeriesQueryResult> getStatusMetric(String instance, ResolutionEnvironment env) {
        Map<String, String> labels = new HashMap<>();
        labels.put(INSTANCE_KEY, instance);

        return graphQLTSDBMetricsService.getMetric(
                env, RESPONSE_TIME_METRIC, labels, TIME_RANGE_IN_SECONDS, TimeRangeUnit.SECOND);
    }

    public Mono<NodeReachability> getNodeReachability(
            NodeDTO node, Integer timeRange, TimeRangeUnit timeRangeUnit, ResolutionEnvironment env) {
        IpInterfaceDTO ipInterface = getPrimaryInterface(node);
        Map<String, String> labels = new HashMap<>();
        var monitoredService = client.getMonitoredService(
                MonitoredServiceQuery.newBuilder()
                        .setMonitoredServiceType(Constants.DEFAULT_MONITOR_TYPE)
                        .setNodeId(node.getId())
                        .setIpAddress(ipInterface.getIpAddress())
                        .setTenantId(ipInterface.getTenantId())
                        .build(),
                headerUtil.getAuthHeader(env));
        labels.put(INSTANCE_KEY, monitoredService.getMonitoredEntityId());

        var request = new MonitoredServiceStatusRequest();
        request.setMonitoredEntityId(monitoredService.getMonitoredEntityId());
        // Defaults to 24hr window for first observation time.
        long firstObservationTime = Instant.now().minus(24, ChronoUnit.HOURS).toEpochMilli();
        try {
            var monitorStatusProto = client.getMonitoredEntityState(request, headerUtil.getAuthHeader(env));
            firstObservationTime = monitorStatusProto.getFirstObservationTime();
        } catch (Exception e) {
            LOG.warn("Exception while getting monitor status for request {}", request);
        }
        var optionalParams = Map.of(Constants.FIRST_OBSERVATION_TIME, String.valueOf(firstObservationTime));
        try {
            return graphQLTSDBMetricsService
                    .getCustomMetric(
                            env, Constants.REACHABILITY_PERCENTAGE, labels, timeRange, timeRangeUnit, optionalParams)
                    .map(result -> transformToNodeReachability(node.getId(), result));
        } catch (Exception e) {
            LOG.warn("Failed to get reachability for node id {}", node.getId(), e);
        }
        return Mono.just(new NodeReachability(node.getId(), 0.0));
    }

    private NodeReachability transformToNodeReachability(long id, TimeSeriesQueryResult result) {
        if (isNull(result)) {
            return new NodeReachability(id, 0.0);
        }
        List<TSResult> tsResults = result.getData().getResult();

        if (isEmpty(tsResults)) {
            return new NodeReachability(id, 0.0);
        }

        for (TSResult tsResult : tsResults) {
            List<List<Double>> values = tsResult.getValues();
            List<Double> doubles;
            if (isEmpty(values)) {
                doubles = tsResult.getValue();
            } else {
                doubles = values.get(values.size() - 1);
            }
            if (doubles.size() != 2) {
                continue;
            }
            Double reachability = doubles.get(1);
            var roundedValue = Math.min(reachability, 100.0);
            return new NodeReachability(id, roundedValue);
        }
        return new NodeReachability(id, 0.0);
    }

    public Mono<NodeResponseTime> getNodeAvgResponseTime(
            NodeDTO node, Integer timeRange, TimeRangeUnit timeRangeUnit, ResolutionEnvironment env) {
        IpInterfaceDTO ipInterface = getPrimaryInterface(node);
        Map<String, String> labels = new HashMap<>();
        var monitoredService = client.getMonitoredService(
                MonitoredServiceQuery.newBuilder()
                        .setMonitoredServiceType(Constants.DEFAULT_MONITOR_TYPE)
                        .setNodeId(node.getId())
                        .setIpAddress(ipInterface.getIpAddress())
                        .setTenantId(ipInterface.getTenantId())
                        .build(),
                headerUtil.getAuthHeader(env));
        labels.put(INSTANCE_KEY, monitoredService.getMonitoredEntityId());

        try {
            return graphQLTSDBMetricsService
                    .getMetric(env, AVG_RESPONSE_TIME, labels, timeRange, timeRangeUnit)
                    .map(result -> transformToNodeResponseTime(node.getId(), result));
        } catch (Exception e) {
            LOG.warn("Failed to get response time for node id {}", node.getId(), e);
        }
        return Mono.just(new NodeResponseTime(node.getId(), 0.0));
    }

    public Mono<TopNNode> getTopNNode(
            NodeDTO nodeDTO, Integer timeRange, TimeRangeUnit timeRangeUnit, ResolutionEnvironment env) {

        Mono<NodeReachability> nodeReachability = getNodeReachability(nodeDTO, timeRange, timeRangeUnit, env);
        Mono<NodeResponseTime> nodeResponseTime = getNodeAvgResponseTime(nodeDTO, timeRange, timeRangeUnit, env);
        Mono<Tuple2<NodeReachability, NodeResponseTime>> result = nodeReachability.zipWith(nodeResponseTime);

        return result.map(tuple -> {
            var topNNode = new TopNNode();
            topNNode.setNodeLabel(nodeDTO.getNodeLabel());
            topNNode.setNodeAlias(nodeDTO.getNodeAlias());
            topNNode.setLocation(nodeDTO.getLocation());
            // Round to 2 points after decimal
            topNNode.setReachability(Math.round(tuple.getT1().getReachability() * 100.0) / 100.0);
            topNNode.setAvgResponseTime(Math.round(tuple.getT2().getResponseTime() * 100.0) / 100.0);
            return topNNode;
        });
    }

    public Mono<NodeStatus> getNodeStatus(NodeDTO nodeDTO, ResolutionEnvironment env) {
        return getNodeStatus(nodeDTO.getId(), Constants.DEFAULT_MONITOR_TYPE, env);
    }

    private NodeResponseTime transformToNodeResponseTime(long id, TimeSeriesQueryResult result) {
        if (isNull(result)) {
            return new NodeResponseTime(id, 0.0);
        }
        List<TSResult> tsResults = result.getData().getResult();

        if (isEmpty(tsResults)) {
            return new NodeResponseTime(id, 0.0);
        }

        for (TSResult tsResult : tsResults) {
            List<List<Double>> values = tsResult.getValues();

            List<Double> doubles;
            if (isEmpty(values)) {
                doubles = tsResult.getValue();
            } else {
                doubles = values.get(values.size() - 1);
            }
            if (doubles.size() != 2) {
                continue;
            }
            Double responseTime = doubles.get(1);
            return new NodeResponseTime(id, responseTime);
        }
        return new NodeResponseTime(id, 0.0);
    }
}

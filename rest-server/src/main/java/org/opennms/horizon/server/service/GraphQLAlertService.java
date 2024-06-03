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

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.opennms.horizon.alerts.proto.EventType;
import org.opennms.horizon.server.mapper.alert.AlertMapper;
import org.opennms.horizon.server.mapper.alert.MonitorPolicyMapper;
import org.opennms.horizon.server.model.alerts.Alert;
import org.opennms.horizon.server.model.alerts.AlertCount;
import org.opennms.horizon.server.model.alerts.AlertEventDefinition;
import org.opennms.horizon.server.model.alerts.AlertResponse;
import org.opennms.horizon.server.model.alerts.CountAlertResponse;
import org.opennms.horizon.server.model.alerts.DeleteAlertResponse;
import org.opennms.horizon.server.model.alerts.EventDefinitionsByVendor;
import org.opennms.horizon.server.model.alerts.EventDefsByVendorRequest;
import org.opennms.horizon.server.model.alerts.ListAlertResponse;
import org.opennms.horizon.server.model.alerts.MonitorPolicy;
import org.opennms.horizon.server.model.alerts.TimeRange;
import org.opennms.horizon.server.model.inventory.DownloadFormat;
import org.opennms.horizon.server.model.inventory.DownloadResponse;
import org.opennms.horizon.server.service.grpc.AlertsClient;
import org.opennms.horizon.server.utils.ServerHeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@GraphQLApi
@Service
public class GraphQLAlertService {

    private static final Logger LOG = LoggerFactory.getLogger(GraphQLAlertService.class);

    private final AlertsClient alertsClient;
    private final ServerHeaderUtil headerUtil;
    private final AlertMapper mapper;
    private final MonitorPolicyMapper policyMapper;

    @SuppressWarnings("squid:S107")
    @GraphQLQuery
    public Mono<ListAlertResponse> findAllAlerts(
            @GraphQLArgument(name = "pageSize") Integer pageSize,
            @GraphQLArgument(name = "page") int page,
            @GraphQLArgument(name = "timeRange") TimeRange timeRange,
            @GraphQLArgument(name = "severities") List<String> severities,
            @GraphQLArgument(name = "sortBy") String sortBy,
            @GraphQLArgument(name = "sortAscending") boolean sortAscending,
            @GraphQLArgument(name = "nodeLabel") String nodeLabel,
            @GraphQLEnvironment ResolutionEnvironment env) {
        return Mono.just(alertsClient.listAlerts(
                        pageSize,
                        page,
                        severities,
                        timeRange,
                        sortBy,
                        sortAscending,
                        nodeLabel,
                        headerUtil.getAuthHeader(env)))
                .map(mapper::protoToAlertResponse);
    }

    @GraphQLQuery(
            name = "countAlerts",
            description = "Returns the total count of alerts filtered by severity and time.")
    public Mono<CountAlertResponse> countAlerts(
            @GraphQLArgument(name = "timeRange") TimeRange timeRange,
            @GraphQLArgument(name = "severityFilters") List<String> severityFilters,
            @GraphQLEnvironment ResolutionEnvironment env) {
        return Mono.just(mapper.protoToCountAlertResponse(
                alertsClient.countAlerts(severityFilters, timeRange, headerUtil.getAuthHeader(env))));
    }

    @GraphQLMutation
    public Mono<AlertResponse> acknowledgeAlert(
            @GraphQLArgument(name = "ids") List<Long> ids, @GraphQLEnvironment ResolutionEnvironment env) {
        return Mono.just(
                mapper.protoToAlertResponse(alertsClient.acknowledgeAlert(ids, headerUtil.getAuthHeader(env))));
    }

    @GraphQLMutation
    public Mono<AlertResponse> unacknowledgeAlert(
            @GraphQLArgument(name = "ids") List<Long> ids, @GraphQLEnvironment ResolutionEnvironment env) {
        return Mono.just(
                mapper.protoToAlertResponse(alertsClient.unacknowledgeAlert(ids, headerUtil.getAuthHeader(env))));
    }

    @GraphQLMutation
    public Mono<AlertResponse> escalateAlert(
            @GraphQLArgument(name = "ids") List<Long> ids, @GraphQLEnvironment ResolutionEnvironment env) {
        return Mono.just(mapper.protoToAlertResponse(alertsClient.escalateAlert(ids, headerUtil.getAuthHeader(env))));
    }

    @GraphQLMutation
    public Mono<AlertResponse> clearAlert(
            @GraphQLArgument(name = "ids") List<Long> ids, @GraphQLEnvironment ResolutionEnvironment env) {
        return Mono.just(mapper.protoToAlertResponse(alertsClient.clearAlert(ids, headerUtil.getAuthHeader(env))));
    }

    @GraphQLMutation
    public Mono<DeleteAlertResponse> deleteAlert(
            @GraphQLArgument(name = "ids") List<Long> ids, @GraphQLEnvironment ResolutionEnvironment env) {
        return Mono.just(
                mapper.protoToDeleteAlertResponse(alertsClient.deleteAlert(ids, headerUtil.getAuthHeader(env))));
    }

    @GraphQLMutation
    public Mono<MonitorPolicy> createMonitorPolicy(
            MonitorPolicy policy, @GraphQLEnvironment ResolutionEnvironment env) {
        var monitorPolicy = alertsClient.createMonitorPolicy(policy, headerUtil.getAuthHeader(env));
        return Mono.just(monitorPolicy);
    }

    @GraphQLQuery
    public Flux<MonitorPolicy> listMonitoryPolicies(@GraphQLEnvironment ResolutionEnvironment env) {
        return Flux.fromIterable(alertsClient.listMonitorPolicies(headerUtil.getAuthHeader(env)));
    }

    @GraphQLQuery
    public Mono<DownloadResponse> searchAndDownloadMonitoringPolicies(
            @GraphQLArgument(name = "pageSize") Integer pageSize,
            @GraphQLArgument(name = "page") int page,
            @GraphQLArgument(name = "sortBy") String sortBy,
            @GraphQLArgument(name = "sortAscending") boolean sortAscending,
            @GraphQLEnvironment ResolutionEnvironment env,
            @GraphQLArgument(name = "downloadFormat") DownloadFormat downloadFormat) {

        return Mono.just(generateDownloadableMonitoringPoliciesResponse(
                alertsClient
                        .downloadMonitorPolicies(pageSize, page, sortBy, sortAscending, headerUtil.getAuthHeader(env))
                        .getPoliciesList()
                        .stream()
                        .map(policyMapper::map)
                        .collect(Collectors.toList()),
                downloadFormat,
                env));
    }

    @GraphQLQuery
    public Mono<MonitorPolicy> findMonitorPolicyById(Long id, @GraphQLEnvironment ResolutionEnvironment env) {
        return Mono.just(alertsClient.getMonitorPolicyById(id, headerUtil.getAuthHeader(env)));
    }

    @GraphQLQuery
    public Mono<MonitorPolicy> getDefaultPolicy(@GraphQLEnvironment ResolutionEnvironment env) {
        return Mono.just(alertsClient.getDefaultPolicy(headerUtil.getAuthHeader(env)));
    }

    @GraphQLQuery
    public Flux<AlertEventDefinition> listAlertEventDefinitions(
            @GraphQLArgument EventType eventType, @GraphQLEnvironment ResolutionEnvironment env) {
        return Flux.fromIterable(alertsClient.listAlertEventDefinitions(eventType, headerUtil.getAuthHeader(env)));
    }

    @GraphQLQuery(name = "alertEventDefsByVendor")
    public Mono<EventDefinitionsByVendor> listEventDefinitionsByVendor(
            @GraphQLArgument EventDefsByVendorRequest request, @GraphQLEnvironment ResolutionEnvironment env) {

        EventDefinitionsByVendor eventsDefinitionsByVendor =
                alertsClient.listAlertEventDefinitionsByVendor(request, headerUtil.getAuthHeader(env));
        eventsDefinitionsByVendor.getAlertEventDefinitionList().stream().forEach(aed -> {
            if (aed.getUei() != null && aed.getUei().contains("/")) {
                String lastElement = Arrays.stream(aed.getUei().split("/"))
                        .reduce((first, second) -> second)
                        .orElse(null);
                aed.setName(lastElement);
            }
            aed.setClearKey(aed.getClearKey().isEmpty() ? null : aed.getClearKey());
            aed.setReductionKey(aed.getReductionKey().isEmpty() ? null : aed.getReductionKey());
        });

        return Mono.just(eventsDefinitionsByVendor);
    }

    @GraphQLQuery(name = "listVendors")
    public Flux<String> listVendors(
            @GraphQLArgument EventType eventType, @GraphQLEnvironment ResolutionEnvironment env) {
        return Flux.fromIterable(alertsClient.listVendors(headerUtil.getAuthHeader(env)));
    }

    @GraphQLMutation
    public Mono<Boolean> deletePolicyById(Long id, @GraphQLEnvironment ResolutionEnvironment env) {
        return Mono.just(alertsClient.deletePolicyById(id, headerUtil.getAuthHeader(env)));
    }

    @GraphQLMutation
    public Mono<Boolean> deleteRuleById(Long id, @GraphQLEnvironment ResolutionEnvironment env) {
        return Mono.just(alertsClient.deleteRuleById(id, headerUtil.getAuthHeader(env)));
    }

    @GraphQLQuery
    public Mono<Long> countAlertByPolicyId(Long id, @GraphQLEnvironment ResolutionEnvironment env) {
        return Mono.just(alertsClient.countAlertByPolicyId(id, headerUtil.getAuthHeader(env)));
    }

    @GraphQLQuery
    public Mono<Long> countAlertByRuleId(Long id, @GraphQLEnvironment ResolutionEnvironment env) {
        return Mono.just(alertsClient.countAlertByRuleId(id, headerUtil.getAuthHeader(env)));
    }

    @GraphQLQuery(name = "alertCounts")
    public Mono<AlertCount> getAlertCounts(@GraphQLEnvironment ResolutionEnvironment env) {
        return Mono.just(alertsClient.countAlerts(headerUtil.getAuthHeader(env)));
    }

    @GraphQLQuery(name = "getAlertsByNode")
    public Mono<ListAlertResponse> getRecentAlertsByNode(
            @GraphQLArgument(name = "pageSize") Integer pageSize,
            @GraphQLArgument(name = "page") int page,
            @GraphQLArgument(name = "sortBy") String sortBy,
            @GraphQLArgument(name = "sortAscending") boolean sortAscending,
            @GraphQLArgument(name = "nodeId") long nodeId,
            @GraphQLEnvironment ResolutionEnvironment env) {

        return Mono.just(alertsClient.getAlertsByNode(
                        pageSize, page, sortBy, sortAscending, nodeId, headerUtil.getAuthHeader(env)))
                .map(mapper::protoToAlertResponse);
    }

    @GraphQLQuery(name = "downloadRecentAlertsByNode")
    public Mono<DownloadResponse> downloadRecentAlertsByNode(
            @GraphQLArgument(name = "pageSize") Integer pageSize,
            @GraphQLArgument(name = "page") int page,
            @GraphQLArgument(name = "sortBy") String sortBy,
            @GraphQLArgument(name = "sortAscending") boolean sortAscending,
            @GraphQLArgument(name = "nodeId") long nodeId,
            @GraphQLEnvironment ResolutionEnvironment env,
            @GraphQLArgument(name = "downloadFormat") DownloadFormat downloadFormat) {

        List<Alert> alerts = alertsClient
                .getAlertsByNode(pageSize, page, sortBy, sortAscending, nodeId, headerUtil.getAuthHeader(env))
                .getAlertsList()
                .stream()
                .map(mapper::protoToAlert)
                .collect(Collectors.toList());

        try {
            return Mono.just(generateDownloadableAlertsResponse(alerts, downloadFormat));
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to download Recent Alert List");
        }
    }

    @GraphQLQuery(name = "getNodesCountByMonitoringPolicy")
    public Mono<Long> getNodesCountByMonitoringPolicy(
            @GraphQLArgument(name = "id") long id, @GraphQLEnvironment ResolutionEnvironment env) {
        return Mono.just(alertsClient.getNodesCountByMonitoringPolicy(id, headerUtil.getAuthHeader(env)));
    }

    private DownloadResponse generateDownloadableMonitoringPoliciesResponse(
            List<MonitorPolicy> monitorPolicies, DownloadFormat downloadFormat, ResolutionEnvironment env) {
        downloadFormat = downloadFormat == null ? DownloadFormat.CSV : downloadFormat;
        if (downloadFormat.equals(DownloadFormat.CSV)) {
            StringBuilder csvData = new StringBuilder();
            var csvformat = CSVFormat.Builder.create()
                    .setHeader("Name", "Description", "Alert Rules", "Affected Nodes")
                    .build();

            try (CSVPrinter csvPrinter = new CSVPrinter(csvData, csvformat)) {
                for (MonitorPolicy policy : monitorPolicies) {
                    csvPrinter.printRecord(
                            policy.getName(),
                            policy.getMemo(),
                            policy.getRules().size(),
                            alertsClient.getNodesCountByMonitoringPolicy(
                                    policy.getId(), headerUtil.getAuthHeader(env)));
                }
                csvPrinter.flush();
            } catch (Exception e) {
                LOG.error("Exception while printing records", e);
            }
            return new DownloadResponse(csvData.toString().getBytes(StandardCharsets.UTF_8), downloadFormat);
        }
        throw new IllegalArgumentException("Invalid download format" + downloadFormat.value);
    }

    private static DownloadResponse generateDownloadableAlertsResponse(
            List<Alert> alertList, DownloadFormat downloadFormat) throws IOException {
        if (downloadFormat == null) {
            downloadFormat = DownloadFormat.CSV;
        }
        if (downloadFormat.equals(DownloadFormat.CSV)) {
            StringBuilder csvData = new StringBuilder();
            var csvformat = CSVFormat.Builder.create()
                    .setHeader("Node Name", "Alert Type", "Severity", "Description", "Last Updated", "Acknowledged")
                    .build();

            try (CSVPrinter csvPrinter = new CSVPrinter(csvData, csvformat)) {
                for (Alert alert : alertList) {
                    csvPrinter.printRecord(
                            alert.getNodeName(),
                            alert.getType(),
                            alert.getSeverity(),
                            alert.getDescription(),
                            new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a z").format(alert.getLastUpdateTimeMs()),
                            alert.isAcknowledged());
                }
                csvPrinter.flush();
            } catch (Exception e) {
                LOG.error("Exception while printing records", e);
            }
            return new DownloadResponse(csvData.toString().getBytes(StandardCharsets.UTF_8), downloadFormat);
        }
        throw new IllegalArgumentException("Invalid download format" + downloadFormat.value);
    }
}

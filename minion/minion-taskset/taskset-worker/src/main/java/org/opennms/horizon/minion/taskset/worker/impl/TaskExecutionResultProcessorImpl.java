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
package org.opennms.horizon.minion.taskset.worker.impl;

import com.google.protobuf.Any;
import java.util.Collections;
import java.util.Optional;
import org.opennms.horizon.minion.plugin.api.CollectionSet;
import org.opennms.horizon.minion.plugin.api.ScanResultsResponse;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorRequest;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorResponse;
import org.opennms.horizon.minion.taskset.worker.TaskExecutionResultProcessor;
import org.opennms.horizon.shared.ipc.rpc.IpcIdentity;
import org.opennms.horizon.shared.ipc.sink.api.SyncDispatcher;
import org.opennms.taskset.contract.CollectorResponse;
import org.opennms.taskset.contract.Identity;
import org.opennms.taskset.contract.MonitorResponse;
import org.opennms.taskset.contract.ScannerResponse;
import org.opennms.taskset.contract.TaskDefinition;
import org.opennms.taskset.contract.TaskResult;
import org.opennms.taskset.contract.TaskSetResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskExecutionResultProcessorImpl implements TaskExecutionResultProcessor {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(TaskExecutionResultProcessorImpl.class);

    private Logger log = DEFAULT_LOGGER;

    private final SyncDispatcher<TaskSetResults> taskSetSinkDispatcher;
    private final IpcIdentity identity;

    public TaskExecutionResultProcessorImpl(
            SyncDispatcher<TaskSetResults> taskSetSinkDispatcher, IpcIdentity identity) {
        this.taskSetSinkDispatcher = taskSetSinkDispatcher;
        this.identity = identity;
    }

    // ========================================
    // API
    // ----------------------------------------

    @Override
    public void queueSendResult(String id, ScanResultsResponse response) {
        final var scanResponse = formatScanResultsResponse(response);

        TaskSetResults taskSetResults = formatTaskSetResults(id, scanResponse);
        log.info("Scan Status: id = {}, results = {} ", id, response.getResults());
        try {
            taskSetSinkDispatcher.send(taskSetResults);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void queueSendResult(
            TaskDefinition taskDefinition,
            ServiceMonitorRequest serviceMonitorRequest,
            ServiceMonitorResponse response) {
        log.info(
                "Poll Status: id = {}, meid = {}, status = {}; ",
                taskDefinition.getId(),
                serviceMonitorRequest.getMonitoredEntityId(),
                response.getStatus());
        final var monitorResponse = MonitorResponse.newBuilder()
                .setResponseTimeMs(response.getResponseTime())
                .setStatus(Optional.of(response)
                        .map(ServiceMonitorResponse::getStatus)
                        .map(Object::toString)
                        .orElse(MonitorResponse.getDefaultInstance().getStatus()))
                .setReason(Optional.of(response)
                        .map(ServiceMonitorResponse::getReason)
                        .orElse(MonitorResponse.getDefaultInstance().getReason()))
                .putAllMetrics(Optional.of(response)
                        .flatMap(r -> Optional.ofNullable(r.getAdditionalMetrics()))
                        .orElse(Collections.emptyMap()))
                .setTimestamp(response.getTimestamp())
                .setMonitoredEntityId(serviceMonitorRequest.getMonitoredEntityId())
                .setMonitorType(serviceMonitorRequest.getMonitorType())
                .putAllMetricLabels(taskDefinition.getMetricLabelsMap())
                .build();

        final var taskSetResults = formatTaskSetResults(taskDefinition.getId(), monitorResponse);

        try {
            taskSetSinkDispatcher.send(taskSetResults);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void queueSendResult(TaskDefinition taskDefinition, CollectionSet collectionSet) {
        final var collectorResponse = formatCollectorResponse(taskDefinition, collectionSet);
        TaskSetResults taskSetResults = formatTaskSetResults(taskDefinition.getId(), collectorResponse);
        log.info("Collect Status: id = {}, status = {} ", taskDefinition.getId(), collectionSet.getStatus());
        try {
            taskSetSinkDispatcher.send(taskSetResults);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // ========================================
    // Internals
    // ----------------------------------------

    private TaskSetResults formatTaskSetResults(String id, ScannerResponse scannerResponse) {
        TaskResult taskResult = TaskResult.newBuilder()
                .setId(id)
                .setScannerResponse(scannerResponse)
                .setIdentity(Identity.newBuilder().setSystemId(identity.getId()))
                .build();

        TaskSetResults taskSetResults =
                TaskSetResults.newBuilder().addResults(taskResult).build();

        return taskSetResults;
    }

    private TaskSetResults formatTaskSetResults(String id, MonitorResponse monitorResponse) {
        TaskResult taskResult = TaskResult.newBuilder()
                .setId(id)
                .setMonitorResponse(monitorResponse)
                .setIdentity(Identity.newBuilder().setSystemId(identity.getId()))
                .build();

        TaskSetResults taskSetResults =
                TaskSetResults.newBuilder().addResults(taskResult).build();

        return taskSetResults;
    }

    private ScannerResponse formatScanResultsResponse(ScanResultsResponse response) {
        return ScannerResponse.newBuilder()
                .setResult(Any.pack(Optional.of(response)
                        .map(ScanResultsResponse::getResults)
                        .orElse(ScannerResponse.getDefaultInstance().getResult())))
                .setReason(Optional.of(response)
                        .map(ScanResultsResponse::getReason)
                        .orElse(ScannerResponse.getDefaultInstance().getReason()))
                .build();
    }

    private TaskSetResults formatTaskSetResults(String id, CollectorResponse collectorResponse) {
        TaskResult taskResult = TaskResult.newBuilder()
                .setId(id)
                .setCollectorResponse(collectorResponse)
                .setIdentity(Identity.newBuilder().setSystemId(identity.getId()))
                .build();

        return TaskSetResults.newBuilder().addResults(taskResult).build();
    }

    private CollectorResponse formatCollectorResponse(TaskDefinition taskDefinition, CollectionSet collectionSet) {

        return CollectorResponse.newBuilder()
                .setStatus(collectionSet.getStatus())
                .setNodeId(collectionSet.getNodeId())
                .setIpAddress(collectionSet.getIpAddress())
                .setTimestamp(collectionSet.getTimeStamp())
                .setResult(Any.pack(collectionSet.getResults()))
                .setMonitorType(taskDefinition.getPluginName())
                .build();
    }
}

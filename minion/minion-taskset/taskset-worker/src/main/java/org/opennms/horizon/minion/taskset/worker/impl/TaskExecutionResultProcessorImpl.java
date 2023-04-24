/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022-2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.horizon.minion.taskset.worker.impl;

import com.google.protobuf.Any;
import org.opennms.horizon.minion.plugin.api.CollectionSet;
import org.opennms.horizon.minion.plugin.api.ScanResultsResponse;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorResponse;
import org.opennms.horizon.minion.taskset.worker.TaskExecutionResultProcessor;
import org.opennms.horizon.shared.ipc.rpc.IpcIdentity;
import org.opennms.horizon.shared.ipc.sink.api.SyncDispatcher;
import org.opennms.taskset.contract.CollectorResponse;
import org.opennms.taskset.contract.MonitorResponse;
import org.opennms.taskset.contract.ScannerResponse;
import org.opennms.taskset.contract.TaskResult;
import org.opennms.taskset.contract.TaskSetResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Optional;

public class TaskExecutionResultProcessorImpl implements TaskExecutionResultProcessor {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(TaskExecutionResultProcessorImpl.class);

    private Logger log = DEFAULT_LOGGER;

    private final SyncDispatcher<TaskSetResults> taskSetSinkDispatcher;
    private final IpcIdentity identity;

    public TaskExecutionResultProcessorImpl(SyncDispatcher<TaskSetResults> taskSetSinkDispatcher, IpcIdentity identity) {
        this.taskSetSinkDispatcher = taskSetSinkDispatcher;
        this.identity = identity;
    }


//========================================
// API
//----------------------------------------

    @Override
    public void queueSendResult(String id, ScanResultsResponse response) {
        TaskSetResults taskSetResults = formatTaskSetResults(id, response);
        log.info("Scan Status: id = {}, results = {} ", id, response.getResults());
        taskSetSinkDispatcher.send(taskSetResults);
    }

    @Override
    public void queueSendResult(String id, ServiceMonitorResponse response) {
        log.info("Poll Status: id = {} , status = {}; ", id, response.getStatus());

        TaskSetResults taskSetResults = formatTaskSetResults(id, response);

        taskSetSinkDispatcher.send(taskSetResults);
    }

    @Override
    public void queueSendResult(String id, CollectionSet collectionSet) {
        TaskSetResults taskSetResults = formatTaskSetResults(id, collectionSet);
        log.info("Collect Status: id = {}, status = {} ", id, collectionSet.getStatus());
        taskSetSinkDispatcher.send(taskSetResults);
    }

//========================================
// Internals
//----------------------------------------

    private TaskSetResults formatTaskSetResults(String id, ScanResultsResponse result) {
        ScannerResponse scannerResponse = formatScanResultsResponse(result);

        TaskResult taskResult =
            TaskResult.newBuilder()
                .setId(id)
                .setScannerResponse(scannerResponse)
                .setLocation(identity.getLocation())
                .setSystemId(identity.getId())
                .build();

        TaskSetResults taskSetResults =
            TaskSetResults.newBuilder()
                .addResults(taskResult)
                .build();

        return taskSetResults;
    }

    private TaskSetResults formatTaskSetResults(String id, ServiceMonitorResponse result) {
        MonitorResponse monitorResponse = formatMonitorResponse(result);

        TaskResult taskResult =
            TaskResult.newBuilder()
                .setId(id)
                .setMonitorResponse(monitorResponse)
                .setLocation(identity.getLocation())
                .setSystemId(identity.getId())
                .build();

        TaskSetResults taskSetResults =
            TaskSetResults.newBuilder()
                .addResults(taskResult)
                .build();

        return taskSetResults;
    }


    private ScannerResponse formatScanResultsResponse(ScanResultsResponse response) {
        return ScannerResponse.newBuilder()
            .setResult(Any.pack(Optional.of(response).map(ScanResultsResponse::getResults).orElse(ScannerResponse.getDefaultInstance().getResult())))
            .setReason(Optional.of(response).map(ScanResultsResponse::getReason).orElse(ScannerResponse.getDefaultInstance().getReason()))
            .build();
    }


    private MonitorResponse formatMonitorResponse(ServiceMonitorResponse smr) {
        MonitorResponse result =
            MonitorResponse.newBuilder()
                .setMonitorType(Optional.of(smr).map(ServiceMonitorResponse::getMonitorType).orElse(MonitorResponse.getDefaultInstance().getMonitorType()))
                .setIpAddress(Optional.of(smr).map(ServiceMonitorResponse::getIpAddress).orElse(MonitorResponse.getDefaultInstance().getIpAddress()))
                .setResponseTimeMs(smr.getResponseTime())
                .setStatus(Optional.of(smr).map(ServiceMonitorResponse::getStatus).map(Object::toString).orElse(MonitorResponse.getDefaultInstance().getStatus()))
                .setReason(Optional.of(smr).map(ServiceMonitorResponse::getReason).orElse(MonitorResponse.getDefaultInstance().getReason()))
                .putAllMetrics(Optional.of(smr).map(ServiceMonitorResponse::getProperties).orElse(Collections.EMPTY_MAP))
                .setNodeId(smr.getNodeId())
                .setTimestamp(smr.getTimestamp())
                .build();

        return result;
    }

    private TaskSetResults formatTaskSetResults(String id, CollectionSet collectionSet) {
        CollectorResponse collectorResponse = formatCollectorResponse(collectionSet);

        TaskResult taskResult =
            TaskResult.newBuilder()
                .setId(id)
                .setCollectorResponse(collectorResponse)
                .setLocation(identity.getLocation())
                .setSystemId(identity.getId())
                .build();

        return TaskSetResults.newBuilder()
            .addResults(taskResult)
            .build();

    }

    private CollectorResponse formatCollectorResponse(CollectionSet collectionSet) {

        return CollectorResponse.newBuilder()
            .setStatus(collectionSet.getStatus())
            .setNodeId(collectionSet.getNodeId())
            .setIpAddress(collectionSet.getIpAddress())
            .setMonitorType(collectionSet.getMonitorType())
            .setResult(Any.pack(collectionSet.getResults())).build();
    }


}

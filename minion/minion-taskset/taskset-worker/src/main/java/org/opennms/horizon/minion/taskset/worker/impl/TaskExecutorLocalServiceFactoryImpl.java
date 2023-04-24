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

import org.opennms.horizon.minion.plugin.api.registries.ScannerRegistry;
import org.opennms.horizon.minion.plugin.api.registries.CollectorRegistry;
import org.opennms.horizon.minion.plugin.api.registries.DetectorRegistry;
import org.opennms.horizon.minion.plugin.api.registries.ListenerFactoryRegistry;
import org.opennms.horizon.minion.plugin.api.registries.MonitorRegistry;
import org.opennms.horizon.minion.scheduler.OpennmsScheduler;
import org.opennms.horizon.minion.taskset.worker.TaskExecutionResultProcessor;
import org.opennms.horizon.minion.taskset.worker.TaskExecutorLocalService;
import org.opennms.horizon.minion.taskset.worker.TaskExecutorLocalServiceFactory;
import org.opennms.taskset.contract.TaskDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskExecutorLocalServiceFactoryImpl implements TaskExecutorLocalServiceFactory {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(TaskExecutorLocalServiceFactoryImpl.class);

    private Logger log = DEFAULT_LOGGER;

    private final OpennmsScheduler scheduler;
    private final TaskExecutionResultProcessor resultProcessor;
    private final ListenerFactoryRegistry listenerFactoryRegistry;
    private final DetectorRegistry detectorRegistry;
    private final MonitorRegistry monitorRegistry;
    private final CollectorRegistry collectorRegistry;
    private final ScannerRegistry scannerRegistry;

//========================================
// Constructor
//----------------------------------------

    public TaskExecutorLocalServiceFactoryImpl(
        OpennmsScheduler scheduler,
        TaskExecutionResultProcessor resultProcessor,
        ListenerFactoryRegistry listenerFactoryRegistry,
        DetectorRegistry detectorRegistry,
        MonitorRegistry monitorRegistry,
        CollectorRegistry collectorRegistry,
        ScannerRegistry scannerRegistry) {

        this.scheduler = scheduler;
        this.resultProcessor = resultProcessor;
        this.listenerFactoryRegistry = listenerFactoryRegistry;
        this.detectorRegistry = detectorRegistry;
        this.monitorRegistry = monitorRegistry;
        this.collectorRegistry = collectorRegistry;
        this.scannerRegistry = scannerRegistry;
    }

//========================================
// API
//----------------------------------------

    @Override
    public TaskExecutorLocalService create(TaskDefinition taskDefinition) {

        switch (taskDefinition.getType()) {
            case SCANNER:
                return new TaskExecutorLocalScannerServiceImpl(taskDefinition, scannerRegistry, resultProcessor);

            case MONITOR:
                return new TaskExecutorLocalMonitorServiceImpl(scheduler, taskDefinition, resultProcessor, monitorRegistry);

            case LISTENER:
                TaskListenerRetryable listenerService = new TaskListenerRetryable(taskDefinition, resultProcessor, listenerFactoryRegistry);
                return new TaskCommonRetryExecutor(scheduler, taskDefinition, resultProcessor, listenerService);

            case CONNECTOR:
                TaskConnectorRetryable connectorService = new TaskConnectorRetryable(taskDefinition, resultProcessor);
                return new TaskCommonRetryExecutor(scheduler, taskDefinition, resultProcessor, connectorService);

            case COLLECTOR:
                return new TaskExecutorLocalCollectorServiceImpl(taskDefinition, scheduler, resultProcessor, collectorRegistry);

            default:
                throw new RuntimeException("unrecognized taskDefinition type " + taskDefinition.getType());
        }
    }
}

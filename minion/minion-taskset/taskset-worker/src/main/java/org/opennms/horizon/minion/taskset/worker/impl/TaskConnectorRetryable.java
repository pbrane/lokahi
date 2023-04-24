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

import org.apache.ignite.resources.SpringResource;
import org.opennms.horizon.minion.plugin.api.registries.ServiceConnectorFactoryRegistry;
import com.google.protobuf.Any;
import org.opennms.horizon.minion.taskset.worker.RetryableExecutor;
import org.opennms.horizon.minion.taskset.worker.TaskExecutionResultProcessor;
import org.opennms.horizon.minion.plugin.api.ServiceConnector;
import org.opennms.horizon.minion.plugin.api.ServiceConnectorFactory;
import org.opennms.taskset.contract.TaskDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connector Service
 */
public class TaskConnectorRetryable implements RetryableExecutor {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(TaskConnectorRetryable.class);

    private Logger log = DEFAULT_LOGGER;

    @SpringResource(resourceClass = ServiceConnectorFactoryRegistry.class)
    private transient ServiceConnectorFactoryRegistry serviceConnectorFactoryRegistry;

    private TaskDefinition taskDefinition;
    private TaskExecutionResultProcessor resultProcessor;
    private ServiceConnector serviceConnector;

    private Runnable onDisconnect;

    public TaskConnectorRetryable(TaskDefinition taskDefinition, TaskExecutionResultProcessor resultProcessor) {
        this.taskDefinition = taskDefinition;
        this.resultProcessor = resultProcessor;
    }

//========================================
// API
//----------------------------------------

    @Override
    public void init(Runnable handleRetryNeeded) {
        this.onDisconnect = handleRetryNeeded;
    }

    @Override
    public void attempt(Any config) throws Exception {
        ServiceConnectorFactory serviceConnectorFactory = lookupServiceConnectorFactory(taskDefinition);

        serviceConnector =
                serviceConnectorFactory.create(
                        result -> resultProcessor.queueSendResult(taskDefinition.getId(), result),
                        config,
                        onDisconnect
                );

        log.info("Attempting to connect: workflow-uuid={}", taskDefinition.getId());
        serviceConnector.connect();
    }

    @Override
    public void cancel() {
        serviceConnector.disconnect();
    }

//========================================
// Setup Internals
//----------------------------------------

    private ServiceConnectorFactory lookupServiceConnectorFactory(TaskDefinition workflow) throws Exception {
        String pluginName = workflow.getPluginName();

        ServiceConnectorFactory result = serviceConnectorFactoryRegistry.getService(pluginName);

        if (result == null) {
            log.error("Failed to locate connector factory for workflow: plugin-name={}; workflow-uuid={}",
                    pluginName, workflow.getId());
            throw new Exception("Failed to locate connector factory for workflow: plugin-name=" + pluginName);
        }

        return result;
    }
}

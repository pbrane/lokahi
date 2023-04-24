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

import org.apache.ignite.IgniteLogger;
import org.apache.ignite.resources.LoggerResource;
import org.apache.ignite.resources.SpringResource;
import org.apache.ignite.services.Service;
import org.opennms.horizon.minion.taskset.worker.TaskExecutorLocalService;
import org.opennms.horizon.minion.taskset.worker.TaskExecutorLocalServiceFactory;
import org.opennms.taskset.contract.TaskDefinition;

/**
 * Ignite version of the service to execute workflows.  Uses the "local" version of the service,
 *  WorkflowExecutorLocalService, which is never serialized/deserialized, reducing the challenges that introduces.
 */
public class TaskExecutorIgniteService implements Service {

    private TaskDefinition taskDefinition;

    @LoggerResource
    private IgniteLogger logger;

    @SpringResource(resourceClass = TaskExecutorLocalServiceFactory.class)
    private transient TaskExecutorLocalServiceFactory workflowExecutorLocalServiceFactory;

    private transient TaskExecutorLocalService localService;
    private transient boolean shutdown;

    private transient Object sync;

    public TaskExecutorIgniteService(TaskDefinition taskDefinition) {
        this.taskDefinition = taskDefinition;
    }

//========================================
// Ignite Service API
//----------------------------------------

    @Override
    public void init() throws Exception {
        sync = new Object();
        shutdown = false;
    }

    @Override
    public void execute() throws Exception {
        if (shutdown) {
            logger.info("Skipping execution of workflow; appears to have been canceled already");
            return;
        }

        TaskExecutorLocalService newLocalService = workflowExecutorLocalServiceFactory.create(taskDefinition);
        synchronized (sync) {
            if (! shutdown) {
                localService = newLocalService;
                localService.start();
            } else {
                logger.info("Aborting execution of workflow; appears to have been canceled before fully started");
            }
        }
    }

    @Override
    public void cancel() {
        TaskExecutorLocalService shutdownService = null;

        synchronized (sync) {
            if (! shutdown) {
                shutdownService = localService;
            }
            shutdown = true;
            localService = null;
        }

        if (shutdownService != null) {
            shutdownService.cancel();
        }
    }
}

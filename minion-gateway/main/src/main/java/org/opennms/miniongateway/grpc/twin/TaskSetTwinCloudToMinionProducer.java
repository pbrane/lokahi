/*
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
 */

package org.opennms.miniongateway.grpc.twin;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.opennms.miniongateway.client.TaskSetClient;
import org.opennms.miniongateway.client.TaskSetListener;
import org.opennms.miniongateway.grpc.server.model.TenantKey;
import org.opennms.miniongateway.grpc.twin.TwinPublisher.Session;
import org.opennms.taskset.contract.TaskSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskSetTwinCloudToMinionProducer implements CloudToMinionProducer, TaskSetListener {

    private final Logger logger = LoggerFactory.getLogger(TaskSetTwinCloudToMinionProducer.class);

    private final Session<TaskSet> session;
    private final String tenantId;
    private String location;

    public TaskSetTwinCloudToMinionProducer(Session<TaskSet> session, String tenantId, String location, TaskSetClient taskSetClient) {
        this.session = session;
        this.tenantId = tenantId;
        this.location = location;
        taskSetClient.subscribe(tenantId, location, this);
        taskSetClient.getTaskSet(tenantId, location).whenComplete((result, error) -> {
            if (error != null || result == null) {
                logger.info("Taskset service does not hold a copy of document for tenant-id: {}, location={}", tenantId, location);
                // not found
                return;
            }
            publish(result);
        });
    }

    @Override
    public void close() throws Exception {
        session.close();
    }

    @Override
    public void created(TenantKey tenantKey, TaskSet taskSet) {
        publish(taskSet);
    }

    @Override
    public void updated(TenantKey tenantKey, TaskSet taskSet) {
        publish(taskSet);
    }

    @Override
    public void deleted(TenantKey tenantKey, TaskSet taskSet) {
//        try {
//            session.delete(tenantId, taskSet);
//        } catch (IOException e) {
//            logger.warn("Failed to publish taskset tenant-id: {}, location={}", tenantId, location);
//        }
    }

    @Override
    public void closed() throws Exception {
        session.close();
    }

    @Override
    public void failure(Throwable throwable) {
        try {
            session.close();
        } catch (Exception e) {
            logger.info("Failed to close session while handling stream error {}", e, throwable);
        }
    }

    private void publish(TaskSet taskSet) {
        try {
            session.publish(tenantId, taskSet);
        } catch (IOException e) {
            logger.info("Error while publishing taskset for tenant-id: {}, location={}", tenantId, location);
        }
    }

}

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

import io.grpc.stub.StreamObserver;
import java.io.IOException;
import org.opennms.cloud.grpc.minion.CloudToMinionMessage;
import org.opennms.cloud.grpc.minion.Identity;
import org.opennms.horizon.shared.grpc.common.TenantIDGrpcServerInterceptor;
import org.opennms.miniongateway.client.TaskSetClient;
import org.opennms.miniongateway.grpc.server.ConnectionIdentity;
import org.opennms.miniongateway.grpc.twin.TwinPublisher.Session;
import org.opennms.taskset.contract.TaskSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskSetTwinCloudToMinionProducerFactory implements CloudToMinionProducerFactory {
    private final Logger logger = LoggerFactory.getLogger(TaskSetTwinCloudToMinionProducerFactory.class);
    private final TaskSetClient taskSetClient;
    private final GrpcTwinPublisher twinPublisher;

    private final TenantIDGrpcServerInterceptor tenantIDGrpcServerInterceptor;


    public TaskSetTwinCloudToMinionProducerFactory(TaskSetClient taskSetClient, GrpcTwinPublisher twinPublisher,
        TenantIDGrpcServerInterceptor tenantIDGrpcServerInterceptor) {
        this.taskSetClient = taskSetClient;
        this.twinPublisher = twinPublisher;
        this.tenantIDGrpcServerInterceptor = tenantIDGrpcServerInterceptor;
    }

    @Override
    public CloudToMinionProducer create(Identity identity, StreamObserver<CloudToMinionMessage> responseObserver) {
        // important to register stream!
        twinPublisher.registerConnection(new ConnectionIdentity(identity), responseObserver);
        // open session
        String tenantId = tenantIDGrpcServerInterceptor.readCurrentContextTenantId();
        logger.info("Have Message to send to Minion: tenant-id: {}; system-id={}, location={}",
            tenantId,
            identity.getSystemId(),
            identity.getLocation());

        try {
            Session<TaskSet> session = twinPublisher.register("task-set", TaskSet.class, tenantId,
                identity.getLocation());
            return new TaskSetTwinCloudToMinionProducer(session, tenantId, identity.getLocation(), taskSetClient);
        } catch (IOException e) {
            responseObserver.onError(e);
            logger.info("Could not handles incoming minion system-id: {}, location={} connection", identity.getSystemId(),
                identity.getLocation(), e);
            return null;
        }
    }

}

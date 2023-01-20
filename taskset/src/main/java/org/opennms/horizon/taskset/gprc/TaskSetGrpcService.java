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

package org.opennms.horizon.taskset.gprc;

import com.swrve.ratelimitedlogger.RateLimitedLog;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.opennms.horizon.shared.grpc.common.TenantIDGrpcServerInterceptor;
import org.opennms.horizon.taskset.persistence.TaskSetPersistentStore;
import org.opennms.horizon.taskset.persistence.TaskSetPersistentStore.Listener;
import org.opennms.taskset.contract.TaskSet;
import org.opennms.taskset.service.contract.FetchTaskSetRequest;
import org.opennms.taskset.service.contract.Operation;
import org.opennms.taskset.service.contract.PublishTaskSetRequest;
import org.opennms.taskset.service.contract.PublishTaskSetResponse;
import org.opennms.taskset.service.contract.TaskSetServiceGrpc;
import org.opennms.taskset.service.contract.TaskSetStreamMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskSetGrpcService extends TaskSetServiceGrpc.TaskSetServiceImplBase {

    private final Logger logger = LoggerFactory.getLogger(TaskSetGrpcService.class);

    private final TaskSetPersistentStore taskSetStore;

    private final TenantIDGrpcServerInterceptor tenantIDGrpcServerInterceptor;

    public TaskSetGrpcService(TaskSetPersistentStore taskSetStore, TenantIDGrpcServerInterceptor tenantIDGrpcServerInterceptor) {
        this.taskSetStore = taskSetStore;
        this.tenantIDGrpcServerInterceptor = tenantIDGrpcServerInterceptor;
    }

    @Override
    public void publishTaskSet(PublishTaskSetRequest request, StreamObserver<PublishTaskSetResponse> responseObserver) {
        // Retrieve the Tenant ID from the TenantID GRPC Interceptor
        String tenantId = tenantIDGrpcServerInterceptor.readCurrentContextTenantId();

        taskSetStore.store(tenantId, request.getLocation(), request.getTaskSet());

        PublishTaskSetResponse response =
            PublishTaskSetResponse.newBuilder()
                .build()
            ;

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void fetchTaskSet(FetchTaskSetRequest request, StreamObserver<TaskSet> responseObserver) {
        String tenantId = tenantIDGrpcServerInterceptor.readCurrentContextTenantId();

        TaskSet taskset = taskSetStore.retrieve(tenantId, request.getLocation());
        if (taskset != null) {
            responseObserver.onNext(taskset);
            responseObserver.onCompleted();
            return;
        }
        responseObserver.onError(Status.NOT_FOUND.asException());
    }

    @Override
    public void subscribe(FetchTaskSetRequest request, StreamObserver<TaskSetStreamMessage> responseObserver) {
        String tenantId = tenantIDGrpcServerInterceptor.readCurrentContextTenantId();

        taskSetStore.addListener(tenantId, request.getLocation(), new Listener() {

            @Override
            public void created(TaskSet taskSet) {
                logger.info("Store emitted taskset created notification for tenant-id: {} and location: {}", tenantId, request.getLocation());
                publish(Operation.CREATE, taskSet, responseObserver);
            }

            @Override
            public void updated(TaskSet taskSet) {
                logger.info("Store emitted updated notification for tenant-id: {} and location: {}", tenantId, request.getLocation());
                publish(Operation.UPDATE, taskSet, responseObserver);
            }

            @Override
            public void removed(TaskSet taskSet) {
                logger.info("Store emitted deleted notification for tenant-id: {} and location: {}", tenantId, request.getLocation());
                publish(Operation.DELETE, taskSet, responseObserver);
            }

            private void publish(Operation create, TaskSet taskSet, StreamObserver<TaskSetStreamMessage> responseObserver) {
                TaskSetStreamMessage message = TaskSetStreamMessage.newBuilder()
                    .setOperation(create)
                    .setTaskSet(taskSet)
                    .build();
                responseObserver.onNext(message);
            }
        });
    }
}

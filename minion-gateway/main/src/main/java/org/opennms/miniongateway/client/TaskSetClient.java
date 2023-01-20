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

package org.opennms.miniongateway.client;

import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.opennms.horizon.shared.grpc.common.TenantIDGrpcClientInterceptor;
import org.opennms.taskset.contract.TaskSet;
import org.opennms.taskset.service.contract.FetchTaskSetRequest;
import org.opennms.taskset.service.contract.TaskSetServiceGrpc;
import org.opennms.taskset.service.contract.TaskSetServiceGrpc.TaskSetServiceStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskSetClient {

    private final Logger logger = LoggerFactory.getLogger(TaskSetClient.class);

    private final TaskSetServiceStub taskSetServiceStub;

    public TaskSetClient(ManagedChannel channel, long deadline) {
        this.taskSetServiceStub = TaskSetServiceGrpc.newStub(channel)
            .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS);
    }

    public CompletableFuture<TaskSet> getTaskSet(String tenantId, String location) {
        CompletableFuture<TaskSet> future = new CompletableFuture<>();
        FetchTaskSetRequest request = FetchTaskSetRequest.newBuilder()
            .setLocation(location)
            .build();
        taskSetServiceStub.withInterceptors(new TenantIDGrpcClientInterceptor(() -> tenantId)).fetchTaskSet(request, new StreamObserver<>() {
            @Override
            public void onNext(TaskSet value) {
                future.complete(value);
            }

            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }

            @Override
            public void onCompleted() {
                logger.trace("Completed read out of Task Set for tenant: {}, location: {}", tenantId, location);
            }
        });
        return future;
    }

    public Closeable subscribe(String tenantId, String location, Consumer<TaskSet> consumer) {
        // taskSetServiceStub.
        return new Closeable() {
            @Override
            public void close() throws IOException {

            }
        };
    }
}

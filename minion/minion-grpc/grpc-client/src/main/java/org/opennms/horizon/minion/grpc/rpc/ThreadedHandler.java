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

package org.opennms.horizon.minion.grpc.rpc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import org.opennms.cloud.grpc.minion.RpcRequestProto;
import org.opennms.cloud.grpc.minion.RpcResponseProto;

public class ThreadedHandler implements RpcRequestHandler {

    private final RpcRequestHandler delegate;
    private final AtomicLong counter = new AtomicLong();
    private final ThreadFactory requestHandlerThreadFactory = (runnable) -> new Thread(runnable, "rpc-request-handler-" + counter.incrementAndGet());
    private final ExecutorService requestHandlerExecutor = Executors.newCachedThreadPool(requestHandlerThreadFactory);

    public ThreadedHandler(RpcRequestHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public CompletableFuture<RpcResponseProto> handle(RpcRequestProto request) {
        CompletableFuture<RpcResponseProto> future = new CompletableFuture<>();
        requestHandlerExecutor.submit(() -> delegate.handle(request).whenComplete((r, e) -> {
            if (e != null) {
                future.completeExceptionally(e);
                return;
            }
            future.complete(r);
        }));
        return future;
    }

    public void shutdown() {
        delegate.shutdown();
        requestHandlerExecutor.shutdown();
    }
}

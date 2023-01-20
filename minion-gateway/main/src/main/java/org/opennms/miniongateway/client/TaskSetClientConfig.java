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
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskSetClientConfig {

    public static final String TASKSET_GRPC_CHANNEL = "taskset";

    @Value("${grpc.client.taskset.host:taskset}")
    private String host;

    @Value("${grpc.client.taskset.port:8990}")
    private int port;

    @Value("${grpc.client.taskset.tlsEnabled:false}")
    private boolean tlsEnabled;

    @Value("${grpc.client.taskset.maxMessageSize:10485760}")
    private int maxMessageSize;

    @Value("${grpc.server.deadline:60000}")
    private long deadline;

    private ManagedChannel createGrpcChannel() {
        NettyChannelBuilder channelBuilder = NettyChannelBuilder.forAddress(host, port)
            .keepAliveWithoutCalls(true)
            .maxInboundMessageSize(maxMessageSize);

        ManagedChannel channel;

        if (tlsEnabled) {
            throw new IllegalArgumentException("TLS NOT YET IMPLEMENTED");
        } else {
            channel = channelBuilder.usePlaintext().build();
        }
        return channel;
    }

    @Bean
    public TaskSetClient taskSetClient() {
        return new TaskSetClient(createGrpcChannel(), deadline);
    }

}

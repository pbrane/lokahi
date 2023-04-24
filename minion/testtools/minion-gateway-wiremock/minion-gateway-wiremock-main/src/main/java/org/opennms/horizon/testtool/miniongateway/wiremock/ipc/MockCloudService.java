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

package org.opennms.horizon.testtool.miniongateway.wiremock.ipc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.opennms.cloud.grpc.minion.CloudServiceGrpc;
import org.opennms.cloud.grpc.minion.CloudToMinionMessage;
import org.opennms.cloud.grpc.minion.Identity;
import org.opennms.cloud.grpc.minion.MinionToCloudMessage;
import org.opennms.cloud.grpc.minion.RpcRequestProto;
import org.opennms.cloud.grpc.minion.RpcResponseProto;
import org.opennms.cloud.grpc.minion.SinkMessage;
import org.opennms.horizon.testtool.miniongateway.wiremock.api.MockGrpcServiceApi;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class MockCloudService extends CloudServiceGrpc.CloudServiceImplBase implements MockGrpcServiceApi {

    private final Map<String, List<StreamObserver<CloudToMinionMessage>>> cloudToLocationStreamMap = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> locationSequenceMap = new ConcurrentHashMap<>();
    private final Map<String, StreamObserver<CloudToMinionMessage>> cloudToMinionStreamMap = new ConcurrentHashMap<>();
    private final Set<Identity> connectedMinions = Collections.synchronizedSet(new HashSet<>());
    @Getter
    private final List<SinkMessage> receivedSinkMessages = new ArrayList<>();

//========================================
// INTERFACE
//----------------------------------------

    @Override
    public void sendMessageToLocation(String location, CloudToMinionMessage message) {
        var streamObserverList = cloudToLocationStreamMap.get(location);
        if ((streamObserverList != null) && (! streamObserverList.isEmpty())) {
            locationSequenceMap.computeIfAbsent(location, key -> new AtomicInteger(0));
            int seq = locationSequenceMap.get(location).getAndIncrement();

            var streamObserver= streamObserverList.get(seq % streamObserverList.size());
            streamObserver.onNext(message);
        } else {
            throw new RuntimeException("don't have a connection for location " + location);
        }
    }

    @Override
    public void sendMessageToMinion(String minionId, CloudToMinionMessage message) {
        StreamObserver<CloudToMinionMessage> streamObserver = cloudToMinionStreamMap.get(minionId);
        if (streamObserver != null) {
            streamObserver.onNext(message);
        } else {
            throw new RuntimeException("don't have a connection for Minion " + minionId);
        }
    }

    @Override
    public List<Identity> getConnectedMinions() {
        return new LinkedList<>(connectedMinions);
    }

//========================================
// GRPC Service Endpoints
//----------------------------------------

    @Override
    public void cloudToMinionMessages(Identity minionIdentity, StreamObserver<CloudToMinionMessage> streamObserver) {
        log.info(
            "Have cloud-to-minion stream connection request: location={}; system-id={}",
            minionIdentity.getLocation(),
            minionIdentity.getSystemId());

        connectedMinions.add(minionIdentity);

        cloudToMinionStreamMap.put(minionIdentity.getSystemId(), streamObserver);
        cloudToLocationStreamMap.compute(minionIdentity.getLocation(),
            (key, list) -> {
                if (list == null) {
                    list = new ArrayList<>();
                }
                list.add(streamObserver);

                return list;
            });
    }

    @Override
    public StreamObserver<MinionToCloudMessage> minionToCloudMessages(StreamObserver<Empty> responseObserver) {
        log.info("Have minion-to-cloud message initiated");

        return new StreamObserver<>() {
            @Override
            public void onNext(MinionToCloudMessage value) {
                log.info("Have minion-to-cloud-message from module {}: twin-request.consumer-key={}",
                    value.getSinkMessage().getModuleId(),
                    value.getTwinRequest().getConsumerKey());
                receivedSinkMessages.add(value.getSinkMessage());
            }

            @Override
            public void onError(Throwable t) {
                log.warn("Have minion-to-cloud-message Exception.", t);
            }

            @Override
            public void onCompleted() {

            }
        };
    }

    public StreamObserver<RpcResponseProto> cloudToMinionRPC(StreamObserver<RpcRequestProto> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(RpcResponseProto value) {
                log.info("cloudToMinionRPC called.");
            }

            @Override
            public void onError(Throwable t) {
                log.warn("cloudToMinionRPC Exception.", t);
            }

            @Override
            public void onCompleted() {

            }
        };
    }
}

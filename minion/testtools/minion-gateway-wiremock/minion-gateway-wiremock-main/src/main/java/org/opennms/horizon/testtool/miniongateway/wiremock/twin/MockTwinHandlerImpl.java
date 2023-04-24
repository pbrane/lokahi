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

package org.opennms.horizon.testtool.miniongateway.wiremock.twin;

import com.google.protobuf.ByteString;
import org.opennms.cloud.grpc.minion.CloudToMinionMessage;
import org.opennms.cloud.grpc.minion.TwinResponseProto;
import org.opennms.horizon.testtool.miniongateway.wiremock.api.MockGrpcServiceApi;
import org.opennms.horizon.testtool.miniongateway.wiremock.api.MockTwinHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class MockTwinHandlerImpl implements MockTwinHandler {

    @Autowired
    private MockGrpcServiceApi grpcOperations;

    private AtomicLong sessionSeq = new AtomicLong(0);

    @Override
    public void publish(String location, String topic, byte[] content) {
        TwinResponseProto twinResponseProto =
            TwinResponseProto.newBuilder()
                .setLocation(location)
                .setConsumerKey(topic)
                .setTwinObject(ByteString.copyFrom(content))
                .setSessionId("mock-twin-handler-session-" + sessionSeq.getAndIncrement())
                .build()
                ;

        CloudToMinionMessage cloudToMinionMessage =
            CloudToMinionMessage.newBuilder()
                .setTwinResponse(twinResponseProto)
                .build()
            ;

        grpcOperations.sendMessageToLocation(location, cloudToMinionMessage);
    }
}

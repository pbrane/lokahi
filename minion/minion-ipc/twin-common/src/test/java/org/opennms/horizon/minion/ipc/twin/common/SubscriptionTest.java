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

package org.opennms.horizon.minion.ipc.twin.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.protobuf.Message;
import java.io.IOException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.opennms.horizon.minion.ipc.twin.common.AbstractTwinSubscriber.Subscription;
import org.opennms.horizon.shared.ipc.rpc.IpcIdentity;
import org.opennms.horizon.shared.protobuf.marshalling.ProtoBufJsonSerializer;
import org.opennms.taskset.contract.TaskDefinition;
import org.opennms.taskset.contract.TaskSet;
import org.opennms.taskset.contract.TaskType;

public class SubscriptionTest {

    Subscription subscription =  new LocalTwinSubscriberImpl(new IpcIdentity() {
        @Override
        public String getId() {
            return "blahId";
        }

        @Override
        public String getLocation() {
            return "blahLocation";
        }
    }).new Subscription("blahKey");

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void sendRpcRequest() {
    }

    @Ignore
    @Test
    public void accept() throws IOException {

        SimpleModule simpleModule = new SimpleModule();

        simpleModule.addSerializer(new ProtoBufJsonSerializer<>(TaskSet.class));

        TaskSet taskSet = TaskSet.newBuilder().addTaskDefinition(TaskDefinition.newBuilder().setType(TaskType.MONITOR).build()).build();

        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.registerModule(simpleModule);

        TwinUpdate twinUpdate = new TwinUpdate();
        twinUpdate.setVersion(1);
        twinUpdate.setPatch(false);
        twinUpdate.setSessionId("blahSessionId");
        byte[] objInBytes = objectMapper.writeValueAsBytes(taskSet);
        twinUpdate.setObject(objInBytes);
        twinUpdate.setKey("blahKey");

        subscription.update(twinUpdate);

        twinUpdate.setPatch(true);
        twinUpdate.setVersion(2);

        subscription.update(twinUpdate);
    }
}

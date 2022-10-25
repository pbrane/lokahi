package org.opennms.horizon.minion.ipc.twin.common;

import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.flipkart.zjsonpatch.JsonDiff;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.horizon.minion.ipc.twin.common.AbstractTwinSubscriber.Subscription;
import org.opennms.horizon.shared.ipc.rpc.IpcIdentity;
import org.opennms.horizon.shared.protobuf.marshalling.ProtoBufJsonSerializer;
import org.opennms.taskset.contract.TaskDefinition;
import org.opennms.taskset.contract.TaskSet;
import org.opennms.taskset.contract.TaskType;

public class SubscriptionTest {
    ObjectMapper objectMapper;

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
    public void accept() throws IOException, Exception {

        SimpleModule simpleModule = new SimpleModule();

        simpleModule.addSerializer(new ProtoBufJsonSerializer<>(TaskSet.class));

        TaskSet taskSet = TaskSet.newBuilder().addTaskDefinition(TaskDefinition.newBuilder().setType(TaskType.MONITOR).build()).build();

        objectMapper = new ObjectMapper();

        objectMapper.registerModule(simpleModule);

        TwinUpdate twinUpdate = new TwinUpdate();
        twinUpdate.setVersion(1);
        twinUpdate.setPatch(false);
        twinUpdate.setSessionId("blahSessionId");
        byte[] objInBytes = objectMapper.writeValueAsBytes(taskSet);
        twinUpdate.setObject(objInBytes);
        twinUpdate.setKey("blahKey");

        subscription.update(twinUpdate);

        TaskSet updatedTaskSet = TaskSet.newBuilder().addTaskDefinition(TaskDefinition.newBuilder().setType(TaskType.DETECTOR).build()).build();

        twinUpdate.setPatch(true);
        twinUpdate.setVersion(2);
        byte[] patchValue = getPatchValue(objInBytes, objectMapper.writeValueAsBytes(updatedTaskSet));
        twinUpdate.setObject(patchValue);

        subscription.update(twinUpdate);

        // check no exceptions thrown
        assertTrue(true);
    }

    private byte[] getPatchValue(byte[] originalObj, byte[] updatedObj) throws Exception {
        JsonNode sourceNode = objectMapper.readTree(originalObj);
        JsonNode targetNode = objectMapper.readTree(updatedObj);
        JsonNode diffNode = JsonDiff.asJson(sourceNode, targetNode);
        return diffNode.toString().getBytes(StandardCharsets.UTF_8);
    }
}

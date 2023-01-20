package org.opennms.horizon.taskset.stepdefs;

import com.google.common.util.concurrent.ListenableFuture;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.java.Log;
import org.assertj.core.api.Assertions;
import org.opennms.taskset.contract.TaskDefinition;
import org.opennms.taskset.contract.TaskSet;
import org.opennms.taskset.service.contract.FetchTaskSetRequest;
import org.opennms.taskset.service.contract.PublishTaskSetRequest;
import org.opennms.taskset.service.contract.PublishTaskSetResponse;
import org.opennms.taskset.service.contract.TaskSetServiceGrpc;
import org.opennms.taskset.service.contract.TaskSetServiceGrpc.TaskSetServiceBlockingStub;
import org.opennms.taskset.service.contract.TaskSetServiceGrpc.TaskSetServiceFutureStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskSetStepDefs {

    private final Logger logger = LoggerFactory.getLogger(TaskSetStepDefs.class);

    private int taskSetGrpcPort;
    private String location;

    private Map<String, String> headers = new HashMap<>();

    @Given("TaskSet GRPC Port in system property {string}")
    public void task_set_grpc_port_in_system_property(String propertyName) {
        String value = System.getProperty(propertyName);
        taskSetGrpcPort = Integer.parseInt(value);

        logger.info("Using TASKSET GRPC PORT {}", taskSetGrpcPort);
    }

    @Given("GRPC header {string} = {string}")
    public void grpc_header(String header, String value) {
        headers.put(header, value);
    }

    @Given("mock location {string}")
    public void mock_location(String location) {
        this.location = location;
    }

    @When("Sample taskset is sent to service")
    public void sample_taskset_is_sent_to_service() throws Exception {
        TaskSet taskSet = TaskSet.newBuilder()
            .addTaskDefinition(TaskDefinition.newBuilder()
                .setId("test")
                .setDescription("test description")
                .build())
            .build();

        commonSendRpcRequest(taskSet);
    }

    @Then("Taskset for location {string} and tenant {string} is available")
    public void taskset_for_location_and_tenant_is_available(String location, String tenant) throws Exception {
        headers.put("tenant-id", tenant);

        TaskSet taskSet = createServiceStub().fetchTaskSet(
            FetchTaskSetRequest.newBuilder()
                .setLocation(location)
                .build());

        Assertions.assertThat(taskSet)
            .isNotNull();
    }

    @Then("Taskset for location {string} and tenant {string} is not available")
    public void taskset_for_location_and_tenant_is_not_available(String location, String tenant) throws Exception {
        headers.put("tenant-id", tenant);


        Assertions.assertThatThrownBy(() -> {
            createServiceStub().fetchTaskSet(
                FetchTaskSetRequest.newBuilder()
                    .setLocation(location)
                    .build());
        }).isInstanceOf(StatusRuntimeException.class).hasMessageContaining("NOT_FOUND");
    }


    private void commonSendRpcRequest(TaskSet taskSet) throws Exception {
        TaskSetServiceBlockingStub stub = createServiceStub();
        PublishTaskSetRequest request = PublishTaskSetRequest.newBuilder()
            .setLocation(location)
            .setTaskSet(taskSet)
            .build();

        stub.publishTaskSet(request);
    }

    private TaskSetServiceBlockingStub createServiceStub() {
        NettyChannelBuilder channelBuilder = NettyChannelBuilder.forAddress("localhost", taskSetGrpcPort).intercept(
            prepareGrpcHeaderInterceptor()
        );

        ManagedChannel channel = channelBuilder.usePlaintext().build();
        channel.getState(true);

        return TaskSetServiceGrpc.newBlockingStub(channel);
    }

    private ClientInterceptor prepareGrpcHeaderInterceptor() {
        return MetadataUtils.newAttachHeadersInterceptor(prepareGrpcHeaders());
    }

    private Metadata prepareGrpcHeaders() {
        Metadata result = new Metadata();
        headers.forEach((key, value) -> result.put(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER), value));

        return result;
    }
}

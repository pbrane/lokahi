package org.opennms.horizon.taskset.stepdefs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.assertj.core.api.Assertions;
import org.opennms.horizon.taskset.grpc.Catcher;
import org.opennms.horizon.taskset.grpc.Collector;
import org.opennms.horizon.taskset.grpc.Fetch;
import org.opennms.taskset.contract.TaskDefinition;
import org.opennms.taskset.contract.TaskSet;
import org.opennms.taskset.service.contract.FetchTaskSetRequest;
import org.opennms.taskset.service.contract.Operation;
import org.opennms.taskset.service.contract.PublishTaskSetRequest;
import org.opennms.taskset.service.contract.PublishTaskSetResponse;
import org.opennms.taskset.service.contract.TaskSetServiceGrpc;
import org.opennms.taskset.service.contract.TaskSetServiceGrpc.TaskSetServiceStub;
import org.opennms.taskset.service.contract.TaskSetStreamMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.shaded.org.awaitility.Awaitility;

public class TaskSetStepDefs {

    private final Logger logger = LoggerFactory.getLogger(TaskSetStepDefs.class);

    private int taskSetGrpcPort;
    private String location;

    private Map<String, String> headers = new HashMap<>();
    private Map<String, Catcher<TaskSetStreamMessage>> subscriptions = new HashMap<>();
    private boolean debug;

    private TaskSetServiceStub clientStub;

    @Given("Debug mode")
    public void debug_mode() {
        debug = true;
    }

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

        Fetch<TaskSet> fetch = new Fetch<>();
        createServiceStub().fetchTaskSet(FetchTaskSetRequest.newBuilder().setLocation(location).build(), fetch);
        TaskSet taskSet = await(fetch.asFuture());

        Assertions.assertThat(taskSet)
            .isNotNull();
    }

    @Then("Taskset for location {string} and tenant {string} is not available")
    public void taskset_for_location_and_tenant_is_not_available(String location, String tenant) throws Exception {
        headers.put("tenant-id", tenant);

        assertThatThrownBy(() -> {
            Fetch<TaskSet> fetch = new Fetch<>();
            createServiceStub().fetchTaskSet(FetchTaskSetRequest.newBuilder().setLocation(location).build(), fetch);
            await(fetch.asFuture());
        }).isNotNull().isInstanceOf(StatusRuntimeException.class)
            .hasMessageContaining("NOT_FOUND");
    }

    @When("Subscription {string} is created")
    public void subscription_is_created(String subscription) {
        Catcher<TaskSetStreamMessage> catcher = new Catcher<>(subscription);
        createServiceStub().subscribe(FetchTaskSetRequest.newBuilder().setLocation(location).build(), catcher);
        subscriptions.put(subscription, catcher);
    }

    @Then("Subscription {string} received {string} notification within {long}ms")
    public void subscription_received_update(String subscription, String type, long delay) throws Exception {
        Catcher<TaskSetStreamMessage> collector = subscriptions.get(subscription);
        TaskSetStreamMessage streamMessage = collector.asFuture().get(delay, TimeUnit.MILLISECONDS);

        switch (type) {
            case "created":
                assertThat(streamMessage.getOperation()).isEqualTo(Operation.CREATE);
                break;
            case "updated":
                assertThat(streamMessage.getOperation()).isEqualTo(Operation.UPDATE);
                break;
            case "deleted":
                assertThat(streamMessage.getOperation()).isEqualTo(Operation.DELETE);
                break;
            default:
                fail("Unsupported notification " + type);
        }
    }

    @Then("Subscription {string} did not receive update within {long}ms")
    public void subscription_did_not_receive_update(String subscription, long delay) {
        Catcher<TaskSetStreamMessage> catcher = subscriptions.get(subscription);

        assertThatThrownBy(() -> {
            catcher.asFuture().get(delay, TimeUnit.MILLISECONDS);
        }).isInstanceOf(TimeoutException.class);
    }

    private void commonSendRpcRequest(TaskSet taskSet) throws Exception {
        Fetch<PublishTaskSetResponse> fetch = new Fetch<>();
        createServiceStub().publishTaskSet(PublishTaskSetRequest.newBuilder().setLocation(location).setTaskSet(taskSet).build(),
            fetch
        );

        await(fetch.asFuture());
    }

    private <T> T await(CompletableFuture<T> future) throws Exception {
        return await(future, 5_000);
    }


    private <T> T await(CompletableFuture<T> future, long delay) throws Exception {
        if (debug) {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw (Exception) e.getCause();
            }
        }

        try {
            return future.get(delay, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            throw (Exception) e.getCause();
        }
    }

    private TaskSetServiceStub createServiceStub() {
        if (clientStub == null) {
            NettyChannelBuilder channelBuilder = NettyChannelBuilder.forAddress("localhost", taskSetGrpcPort);

            ManagedChannel channel = channelBuilder.usePlaintext().build();
            channel.getState(true);

            clientStub = TaskSetServiceGrpc.newStub(channel);
        }
        return clientStub.withInterceptors(prepareGrpcHeaderInterceptor());
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

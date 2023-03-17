package org.opennms.miniongateway.router;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.protobuf.Any;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.opennms.horizon.shared.constants.GrpcConstants;
import org.opennms.horizon.shared.protobuf.marshalling.ProtoBufJsonSerializer;
import org.opennms.icmp.contract.IcmpMonitorRequest;
import org.opennms.taskset.contract.TaskDefinition;
import org.opennms.taskset.contract.TaskContext;
import org.opennms.taskset.contract.TaskSet;
import org.opennms.taskset.contract.TaskSet.Builder;
import org.opennms.taskset.contract.TaskType;
import org.opennms.taskset.service.contract.AddSingleTaskOp;
import org.opennms.taskset.service.contract.PublishTaskSetRequest;
import org.opennms.taskset.service.contract.PublishTaskSetResponse;
import org.opennms.taskset.service.contract.TaskSetServiceGrpc;
import org.opennms.taskset.service.contract.TaskSetServiceGrpc.TaskSetServiceStub;
import org.opennms.taskset.service.contract.UpdateSingleTaskOp;
import org.opennms.taskset.service.contract.UpdateTasksRequest;
import org.opennms.taskset.service.contract.UpdateTasksResponse;

public class TaskSetProducer {

    public static final int MAX_MESSAGE_SIZE = 100 * (1024 * 1024);
    public final static Executor executor = new CustomThreadPoolExecutor();

    public static void main(String[] args) throws Exception {
        TaskSet taskSet = createTaskSet();

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(new ProtoBufJsonSerializer<>(TaskSet.class));
        mapper.registerModule(simpleModule);
        byte[] jsonRpr = mapper.writeValueAsString(taskSet).getBytes();

        System.err.println("Binary length " + (taskSet.toByteArray().length / 1024 / 1024) + "mb, json " + (jsonRpr.length / 1024 / 1024) + "mb, max message size is " + (MAX_MESSAGE_SIZE / 1024 / 1024) + " mb");

        TaskSetServiceStub client = TaskSetServiceGrpc.newStub(createGrpcChannel("127.0.0.1", 8991));

        for (int index = 0; index < 1_0000; index++) {
            String location = "Default";
            if (index > 0) {
                location = "Default" + index;
            }
            if (!sendTaskSet(taskSet, location, client)) {
                System.err.println("Sent " + index + " tasks");
                break;
            }
            //Thread.sleep(250);
            if ((index % 50) == 0) {
                System.out.println("Published " + (index + 1) + " elements");
            }
        }
    }

    private static Boolean sendTaskSet(TaskSet taskSet, String location, TaskSetServiceStub client) throws InterruptedException, ExecutionException {
        System.err.println("Generated taskset with " + taskSet.getTaskDefinitionCount() + " tasks in it");
        System.err.println("Constructed a client " + client + " " + ((ManagedChannel) client.getChannel()).getState(true));

        UpdateTasksRequest.Builder updateRequest = UpdateTasksRequest.newBuilder()
            .setLocation(location);
        //        executor.execute(() -> {
        for (TaskDefinition taskDefinition : taskSet.getTaskDefinitionList()) {
            AddSingleTaskOp op = AddSingleTaskOp.newBuilder()
                .setTaskDefinition(taskDefinition)
                .build();

            updateRequest.addUpdate(UpdateSingleTaskOp.newBuilder()
                .setAddTask(op)
            );
        }
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            client
                //.withDeadlineAfter(10000, TimeUnit.MILLISECONDS)
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(createHeaders()))
                .updateTasks(updateRequest.build(), new StreamObserver<>() {
                    @Override
                    public void onNext(UpdateTasksResponse value) {
                        System.err.println("Published " + value);
                        future.complete(true);
                    }

                    @Override
                    public void onError(Throwable t) {
                        System.err.println("Failed " + t);
                        future.complete(false);
                    }

                    @Override
                    public void onCompleted() {
                        System.err.println("Done!");
                    }
                });

            try {
                return
                    future.get();
            } catch (Exception e) {
                System.err.println("Failed to await for results");
            }
//        });
        return false;
    }

    /*
    minion │ taskDefinition {
    minion │ taskDefinition {
    minion │   id: "ip=192.168.2.1/icmp-monitor"
    minion │   type: MONITOR
    minion │   plugin_name: "ICMPMonitor"
    minion │   schedule: "60000"
    minion │   node_id: 1
    minion │   configuration {
    minion │     type_url: "type.googleapis.com/opennms.icmp.IcmpMonitorRequest"
    minion │     value: "\n\v192.168.2.1\020\240\006 \001(@0\002"
    minion │   }
    minion │ }
    */
    @NotNull
    private static TaskSet createTaskSet() {
        Builder builder = TaskSet.newBuilder();

        int limit = 100_000;
        for (int index = 0; index < limit; index++) {
            builder.addTaskDefinition(TaskDefinition.newBuilder()
                .setId("ip=8.8.4.4/icmp-monitor")
                .setType(TaskType.MONITOR)
                .setPluginName("ICMPMonitor")
                .setSchedule("60000")
                .setContext(TaskContext.newBuilder().setNodeId(index).build())
                .setConfiguration(Any.pack(IcmpMonitorRequest.newBuilder()
                    .setHost("8.8.4.4")
                    .build()
                ))
                .build()
            );
        }
        TaskSet taskSet = builder.build();
        return taskSet;
    }

    private static Metadata createHeaders() {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.TENANT_ID_REQUEST_KEY, "opennms-prime");
        return metadata;
    }


    private static ManagedChannel createGrpcChannel(String host, int port) {
        NettyChannelBuilder channelBuilder = NettyChannelBuilder.forAddress(host, port)
            .keepAliveWithoutCalls(true)
            .maxInboundMessageSize(MAX_MESSAGE_SIZE);

        return channelBuilder.usePlaintext().build();
    }

    static class CustomThreadPoolExecutor extends ThreadPoolExecutor {

        public CustomThreadPoolExecutor() {
            super(2, 5, 30_000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(25));
        }

        public CustomThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }

        @Override
        protected void beforeExecute(Thread t, Runnable r) {
            super.beforeExecute(t, r);
            System.out.println("Perform beforeExecute() logic");
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            if (t != null) {
                System.out.println("Perform exception handler logic");
            }
            System.out.println("Perform afterExecute() logic");
        }
    }
}

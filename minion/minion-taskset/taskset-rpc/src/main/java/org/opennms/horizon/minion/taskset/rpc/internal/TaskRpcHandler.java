package org.opennms.horizon.minion.taskset.rpc.internal;

import com.google.protobuf.InvalidProtocolBufferException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.opennms.cloud.grpc.minion.RpcRequestProto;
import org.opennms.horizon.grpc.task.contract.TaskRequest;
import org.opennms.horizon.minion.plugin.api.MonitoredService;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorManager;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorResponse;
import org.opennms.horizon.minion.plugin.api.registries.MonitorRegistry;
import org.opennms.horizon.shared.ipc.rpc.api.minion.RpcHandler;
import org.opennms.taskset.contract.MonitorResponse;
import org.opennms.taskset.contract.TaskDefinition;
import org.opennms.taskset.contract.TaskResult;
import org.opennms.taskset.contract.TaskResult.Builder;
import org.opennms.taskset.contract.TaskType;

public class TaskRpcHandler implements RpcHandler<TaskRequest, MonitorResponse> {

    private final MonitorRegistry monitorRegistry;

    public TaskRpcHandler(MonitorRegistry monitorRegistry) {
        this.monitorRegistry = monitorRegistry;
    }

    @Override
    public CompletableFuture<MonitorResponse> execute(TaskRequest request) {
        ServiceMonitorManager service = monitorRegistry.getService(request.getPluginName());
        if (service == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Unknown plugin"));
        }
        return service.create().poll(new StubService(), request.getConfiguration(), request.getResilience())
            .thenApply(this::createResult);
    }

    private MonitorResponse createResult(ServiceMonitorResponse response) {
        Builder builder = TaskResult.newBuilder();
        // This probably could be unified with org.opennms.horizon.minion.taskset.worker.impl.TaskExecutionResultProcessorImpl.formatMonitorResponse
        return MonitorResponse.newBuilder()
            .setMonitorType(Optional.of(response).map(ServiceMonitorResponse::getMonitorType).orElse(MonitorResponse.getDefaultInstance().getMonitorType()))
            .setIpAddress(Optional.of(response).map(ServiceMonitorResponse::getIpAddress).orElse(MonitorResponse.getDefaultInstance().getIpAddress()))
            .setResponseTimeMs(response.getResponseTime())
            .setStatus(Optional.of(response).map(ServiceMonitorResponse::getStatus).map(Object::toString).orElse(MonitorResponse.getDefaultInstance().getStatus()))
            .setReason(Optional.of(response).map(ServiceMonitorResponse::getReason).orElse(MonitorResponse.getDefaultInstance().getReason()))
            .putAllMetrics(Optional.of(response).map(ServiceMonitorResponse::getProperties).orElse(Collections.EMPTY_MAP))
            .build();
    }

    @Override
    public String getId() {
        return "task";
    }

    @Override
    public TaskRequest unmarshal(RpcRequestProto request) {
        try {
            TaskRequest definition = request.getPayload().unpack(TaskRequest.class);
            return definition;
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    static class StubService implements MonitoredService {

        @Override
        public String getSvcName() {
            return null;
        }

        @Override
        public String getIpAddr() {
            return null;
        }

        @Override
        public long getNodeId() {
            return 0;
        }

        @Override
        public String getNodeLabel() {
            return null;
        }

        @Override
        public String getNodeLocation() {
            return null;
        }

        @Override
        public InetAddress getAddress() {
            return null;
        }

    }


}

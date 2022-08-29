package org.opennms.core.ipc.grpc.server;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import org.opennms.cloud.grpc.minion.RpcRequestProto;
import org.opennms.cloud.grpc.minion.RpcResponseProto;
import org.opennms.core.ipc.grpc.server.manager.RpcRequestDispatcher;
import org.opennms.horizon.shared.ipc.rpc.api.RpcResponseHandler;
import org.opennms.horizon.shared.ipc.rpc.api.server.CallFactory;
import org.opennms.horizon.shared.ipc.rpc.api.server.CallFactory.Call;
import org.opennms.horizon.shared.ipc.rpc.api.server.CallFactory.CallBuilder;
import org.opennms.horizon.shared.ipc.rpc.api.server.CallFactory.Deserializer;

// A complete rpc request builder and response handler, without RpcModule use.

public class BasicCallFactory implements CallFactory {

    private final RpcRequestDispatcher server;

    public BasicCallFactory(RpcRequestDispatcher server) {
        this.server = server;
    }

    @Override
    public <Request extends Message, Response extends Message> CallBuilder<Response> create(Request request, Deserializer<Any, Response> unmarshaller) {
        return new BasicCallBuilder<>(server, request, unmarshaller);
    }
}

class BasicCallBuilder<T extends Message> implements CallBuilder<T> {

    private final RpcRequestDispatcher server;
    private final Message request;
    private final Deserializer<Any, T> unmarshaller;
    private long ttl = 3600L;
    private String module;
    private String location;
    private String system;

    public BasicCallBuilder(RpcRequestDispatcher server, Message request, Deserializer<Any, T> unmarshaller) {
        this.server = server;
        this.request = request;
        this.unmarshaller = unmarshaller;
    }

    @Override
    public CallBuilder<T> withModule(String module) {
        this.module = module;
        return this;
    }

    @Override
    public CallBuilder<T> withTimeToLive(long ttl) {
        this.ttl = ttl;
        return this;
    }

    @Override
    public CallBuilder<T> withSystem(String system) {
        this.system = system;
        return this;
    }

    @Override
    public CallBuilder<T> withLocation(String location) {
        this.location = location;
        return this;
    }

    @Override
    public Call<T> build() {
        return new BasicCall<>(server, request, unmarshaller, module, system, location, ttl);
    }

}

class BasicCall<T extends Message> implements Call<T> {

    private final RpcRequestDispatcher server;
    private final Message request;
    private final Deserializer<Any, T> unmarshaller;
    private final String module;
    private final String system;
    private final String location;
    private final long ttl;

    public BasicCall(RpcRequestDispatcher server, Message request, Deserializer<Any, T> unmarshaller, String module, String system, String location, long ttl) {
        this.server = server;
        this.request = request;
        this.unmarshaller = unmarshaller;
        this.module = module;
        this.system = system;
        this.location = location;
        this.ttl = ttl;
    }

    @Override
    public CompletableFuture<T> execute() {
        RpcRequestProto build = RpcRequestProto.newBuilder()
            .setModuleId(module)
            .setRpcId(UUID.randomUUID().toString())
            .setSystemId(system)
            .setLocation(location)
            .setPayload(Any.pack(request))
            .setExpirationTime(System.currentTimeMillis() + ttl)
            .build();

        Function<RpcResponseProto, T> responseHandler = response -> {
            try {
                return unmarshaller.apply(response.getPayload());
            } catch (IOException e) {
                throw new RuntimeException("Could not deserialize message", e);
            }
        };

        if (system.isBlank()) {
            return server.dispatch(location, build).thenApply(responseHandler);
        }
        return server.dispatch(location, system, build).thenApply(responseHandler);
    }

}

class BasicRpcResponseHandler implements RpcResponseHandler {

    private final long expirationTime;
    private final String rpcId;
    private final String moduleId;
    private final CompletableFuture<RpcResponseProto> future;

    public BasicRpcResponseHandler(long expirationTime, String rpcId, String moduleId, CompletableFuture<RpcResponseProto> future) {
        this.expirationTime = expirationTime;
        this.rpcId = rpcId;
        this.moduleId = moduleId;
        this.future = future;
    }

    @Override
    public void sendResponse(RpcResponseProto response) {
        if (response == null) {
            future.completeExceptionally(new TimeoutException());
            return;
        }

        future.complete(response);
    }

    @Override
    public boolean isProcessed() {
        return future.isDone();
    }

    @Override
    public String getRpcId() {
        return rpcId;
    }

    @Override
    public String getRpcModuleId() {
        return moduleId;
    }

    @Override
    public int compareTo(Delayed other) {
        long myDelay = getDelay(TimeUnit.MILLISECONDS);
        long otherDelay = other.getDelay(TimeUnit.MILLISECONDS);
        return Long.compare(myDelay, otherDelay);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long now = System.currentTimeMillis();
        return unit.convert(expirationTime - now, TimeUnit.MILLISECONDS);
    }

}

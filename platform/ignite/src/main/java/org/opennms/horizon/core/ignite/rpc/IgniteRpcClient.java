package org.opennms.horizon.core.ignite.rpc;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import java.util.concurrent.CompletableFuture;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.client.IgniteClientFuture;
import org.opennms.cloud.grpc.minion.RpcRequestProto;
import org.opennms.cloud.grpc.minion.RpcResponseProto;
import org.opennms.horizon.shared.ignite.remoteasync.MinionRouterService;
import org.opennms.horizon.shared.ipc.rpc.api.RequestBuilder;
import org.opennms.horizon.shared.ipc.rpc.api.RpcClient;
import org.opennms.horizon.shared.ipc.rpc.api.RpcClientFactory.Deserializer;

public class IgniteRpcClient<T extends Message> implements RpcClient<T> {

    private final IgniteClient client;
    private final Deserializer<T> deserializer;

    public IgniteRpcClient(IgniteClient client, Deserializer<T> deserializer) {
        this.client = client;
        this.deserializer = deserializer;
    }
    @Override
    public CompletableFuture<T> execute(RpcRequestProto request) {
        IgniteClientFuture<RpcResponseProto> future = client.compute()
            .executeAsync2(MinionRouterService.IGNITE_SERVICE_NAME, request);
        return future.toCompletableFuture().thenApply(response -> {
            try {
                return deserializer.deserialize(response);
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public RequestBuilder builder(String module) {
        return new IgniteRequestBuilder(module);
    }
}

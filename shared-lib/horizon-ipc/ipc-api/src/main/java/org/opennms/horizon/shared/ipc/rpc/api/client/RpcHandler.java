package org.opennms.horizon.shared.ipc.rpc.api.client;

import com.google.protobuf.Message;
import java.util.concurrent.CompletableFuture;
import org.opennms.cloud.grpc.minion.RpcRequestProto;

public interface RpcHandler<S extends Message, T extends Message> {

    CompletableFuture<T> execute(S request);

    String getId();

    T unmarshal(RpcRequestProto request);
}

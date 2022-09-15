package org.opennms.core.ipc.grpc.server.manager.rpc;

import java.util.concurrent.CompletableFuture;
import org.opennms.cloud.grpc.minion.RpcRequestProto;
import org.opennms.cloud.grpc.minion.RpcResponseProto;

public interface RpcProxyHandler {

    CompletableFuture<RpcResponseProto> handle(RpcRequestProto request);

}

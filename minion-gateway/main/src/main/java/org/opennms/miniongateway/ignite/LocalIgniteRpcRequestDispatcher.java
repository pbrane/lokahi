package org.opennms.miniongateway.ignite;

import java.util.concurrent.CompletableFuture;
import org.opennms.cloud.grpc.minion.RpcRequestProto;
import org.opennms.cloud.grpc.minion.RpcResponseProto;
import org.opennms.core.ipc.grpc.server.manager.rpc.RpcProxyHandler;
import org.opennms.miniongateway.detector.server.IgniteRpcRequestDispatcher;

public class LocalIgniteRpcRequestDispatcher implements IgniteRpcRequestDispatcher {

    private RpcProxyHandler proxyHandler;

    public LocalIgniteRpcRequestDispatcher(RpcProxyHandler proxyHandler) {
        this.proxyHandler = proxyHandler;
    }

    @Override
    public CompletableFuture<RpcResponseProto> execute(RpcRequestProto request) {
        return proxyHandler.handle(request);
    }

}

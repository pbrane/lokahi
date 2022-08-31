package org.opennms.horizon.minion.snmp.ipc.client.internal;

import java.util.concurrent.CompletableFuture;
import org.opennms.cloud.grpc.minion.RpcRequestProto;
import org.opennms.cloud.grpc.minion.RpcResponseProto;

public interface RpcDispatcher {

    CompletableFuture<RpcResponseProto> execute(RpcRequestProto request);
}

package org.opennms.core.ipc.grpc.client.rpc;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import org.opennms.cloud.grpc.minion.RpcRequestProto;
import org.opennms.cloud.grpc.minion.RpcResponseProto;
import org.opennms.horizon.shared.ipc.rpc.IpcIdentity;
import org.opennms.horizon.shared.ipc.rpc.api.RpcModule;
import org.opennms.horizon.shared.ipc.rpc.api.RpcRequest;
import org.opennms.horizon.shared.ipc.rpc.api.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegacyRpcHandler implements RpcRequestHandler {

    private final Logger logger = LoggerFactory.getLogger(LegacyRpcHandler.class);

    // Maintain the map of RPC modules and their ID.
    private final Map<String, RpcModule<RpcRequest, RpcResponse>> registeredModules = new ConcurrentHashMap<>();
    private IpcIdentity ipcIdentity;

    @Override
    public CompletableFuture<RpcResponseProto> handle(RpcRequestProto requestProto) {
        long currentTime = requestProto.getExpirationTime();
        if (requestProto.getExpirationTime() < currentTime) {
            logger.debug("ttl already expired for the request id = {}, won't process.", requestProto.getRpcId());
            return CompletableFuture.failedFuture(new TimeoutException());
        }
        String moduleId = requestProto.getModuleId();
        if (moduleId.isBlank()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Empty module id"));
        }
        logger.debug("Received RPC request with RpcID:{} for module {}", requestProto.getRpcId(), requestProto.getModuleId());
        RpcModule<RpcRequest, RpcResponse> rpcModule = registeredModules.get(moduleId);
        if (rpcModule == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Could not find requested module id"));
        }

        RpcRequest rpcRequest = rpcModule.unmarshalRequest(requestProto.getPayload());
        CompletableFuture<RpcResponse> future = rpcModule.execute(rpcRequest);
        future.thenApply((rpcResponse) -> {
            // Construct response using the same rpcId;
            Any responsePayload = rpcModule.marshalResponse(rpcResponse);
            RpcResponseProto responseProto = RpcResponseProto.newBuilder()
                .setRpcId(requestProto.getRpcId())
                .setSystemId(ipcIdentity.getId())
                .setLocation(requestProto.getLocation())
                .setModuleId(requestProto.getModuleId())
                .setPayload(responsePayload)
                .build();

            return responseProto;
        });

        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void bind(RpcModule module) throws Exception {
        if (module != null) {
            final RpcModule<RpcRequest, RpcResponse> rpcModule = (RpcModule<RpcRequest, RpcResponse>) module;
            if (registeredModules.containsKey(rpcModule.getId())) {
                logger.warn(" {} module is already registered", rpcModule.getId());
            } else {
                registeredModules.put(rpcModule.getId(), rpcModule);
                logger.info("Registered module {} with gRPC IPC client", rpcModule.getId());
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void unbind(RpcModule module) throws Exception {
        if (module != null) {
            final RpcModule<RpcRequest, RpcResponse> rpcModule = (RpcModule<RpcRequest, RpcResponse>) module;
            registeredModules.remove(rpcModule.getId());
            logger.info("Removing module {} from gRPC IPC client.", rpcModule.getId());
        }
    }

    public void shutdown() {
        registeredModules.clear();
    }
}

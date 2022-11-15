package org.opennms.horizon.inventory.grpc;

import com.google.common.net.InetAddresses;
import com.google.rpc.Code;
import com.google.rpc.Status;
import io.grpc.Context;
import io.grpc.protobuf.StatusProto;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opennms.horizon.inventory.dto.DeviceCreateDTO;
import org.opennms.horizon.inventory.dto.DeviceServiceGrpc;
import org.opennms.horizon.inventory.dto.NodeDTO;
import org.opennms.horizon.inventory.mapper.NodeMapper;
import org.opennms.horizon.inventory.model.Node;
import org.opennms.horizon.inventory.service.NodeService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class DeviceGrpcService extends DeviceServiceGrpc.DeviceServiceImplBase {
    private final NodeService nodeService;
    private final NodeMapper nodeMapper;
    private final TenantLookup tenantLookup;

    @Override
    @Transactional
    public void createDevice(DeviceCreateDTO request, StreamObserver<NodeDTO> responseObserver) {
        validateInput(request, responseObserver);

        Optional<String> tenantId = tenantLookup.lookupTenantId(Context.current());
        Node node = nodeService.createDevice(request, tenantId.get());

        responseObserver.onNext(nodeMapper.modelToDTO(node));
        responseObserver.onCompleted();
    }

    private void validateInput(DeviceCreateDTO request, StreamObserver<NodeDTO> responseObserver) {
        // TODO: Check there isn't a node already with same IpInterface and location
        
        if (request.hasManagementIp() && !InetAddresses.isInetAddress(request.getManagementIp())) {
            Status status = Status.newBuilder()
                .setCode(Code.INVALID_ARGUMENT_VALUE)
                .setMessage("Bad management_ip: " + request.getManagementIp())
                .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
        }
    }
}

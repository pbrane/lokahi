package org.opennms.horizon.server.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.opennms.horizon.inventory.dto.DeviceCreateDTO;
import org.opennms.horizon.inventory.dto.DeviceServiceGrpc;
import org.springframework.stereotype.Component;
import org.opennms.horizon.inventory.dto.NodeDTO;

@Component
public class DeviceGrpcClient {
    public NodeDTO createDevice(DeviceCreateDTO deviceCreateDTO) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 6565)
            .usePlaintext()
            .build();

        DeviceServiceGrpc.DeviceServiceBlockingStub stub = DeviceServiceGrpc.newBlockingStub(channel);

        NodeDTO nodeDTO = stub.createDevice(deviceCreateDTO);

        channel.shutdown();

        return nodeDTO;
    }
}

package org.opennms.horizon.inventory.service.grpc;

import com.google.common.net.InetAddresses;
import com.google.rpc.Code;
import com.google.rpc.Status;
import com.vladmihalcea.hibernate.type.basic.Inet;
import io.grpc.protobuf.StatusProto;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opennms.horizon.inventory.dto.DeviceCreateDTO;
import org.opennms.horizon.inventory.dto.DeviceServiceGrpc;
import org.opennms.horizon.inventory.dto.NodeDTO;
import org.opennms.horizon.inventory.mapper.NodeMapper;
import org.opennms.horizon.inventory.model.IpInterface;
import org.opennms.horizon.inventory.model.MonitoringLocation;
import org.opennms.horizon.inventory.model.Node;
import org.opennms.horizon.inventory.repository.IpInterfaceRepository;
import org.opennms.horizon.inventory.repository.MonitoringLocationRepository;
import org.opennms.horizon.inventory.repository.NodeRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class DeviceGrpcService extends DeviceServiceGrpc.DeviceServiceImplBase {
    private final NodeRepository nodeRepository;
    private final MonitoringLocationRepository monitoringLocationRepository;
    private final IpInterfaceRepository ipInterfaceRepository;
    private final NodeMapper nodeMapper;

    @Override
    @Transactional
    public void createDevice(DeviceCreateDTO request, StreamObserver<NodeDTO> responseObserver) {
        validateInput(request, responseObserver);

        MonitoringLocation monitoringLocation = saveMonitoringLocation(request);
        Node node = saveNode(request, monitoringLocation);
        saveIpInterfaces(request, node, responseObserver);

        responseObserver.onNext(nodeMapper.modelToDTO(node));

        responseObserver.onCompleted();
    }

    private void saveIpInterfaces(DeviceCreateDTO request, Node node, StreamObserver<NodeDTO> responseObserver) {
        if (request.hasManagementIp()) {
            IpInterface ipInterface = new IpInterface();

            ipInterface.setNode(node);
            ipInterface.setTenantId(UUID.fromString(request.getTenantId()));
            ipInterface.setIpAddress(new Inet(request.getManagementIp()));

            ipInterfaceRepository.save(ipInterface);
        }
    }

    private MonitoringLocation saveMonitoringLocation(DeviceCreateDTO request) {
        MonitoringLocation location = new MonitoringLocation();

        location.setTenantId(UUID.fromString(request.getTenantId()));
        location.setLocation(request.getLocation());

        return monitoringLocationRepository.save(location);
    }

    private Node saveNode(DeviceCreateDTO request, MonitoringLocation monitoringLocation) {
        Node node = new Node();

        node.setTenantId(UUID.fromString(request.getTenantId()));
        node.setNodeLabel(request.getLabel());
        node.setCreateTime(LocalDateTime.now());
        node.setMonitoringLocation(monitoringLocation);

        return nodeRepository.save(node);
    }

    private void validateInput(DeviceCreateDTO request, StreamObserver<NodeDTO> responseObserver) {
        boolean validTenant = true;
        boolean validIp = true;

        try {
            UUID.fromString(request.getTenantId());
        } catch (IllegalArgumentException ex) {
            validTenant = false;
        }

        if (request.hasManagementIp()) {
            if (!InetAddresses.isInetAddress(request.getManagementIp())) {
                validIp = false;
            }
        }

        if (!validTenant || !validIp) {
            String message = getErrorMessage(request, validTenant, validIp);

            Status status = Status.newBuilder()
                .setCode(Code.INVALID_ARGUMENT_VALUE)
                .setMessage(message)
                .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
        }
    }

    private String getErrorMessage(DeviceCreateDTO request, boolean validTenant, boolean validIp) {
        String message = "";
        if (!validTenant) {
            message += "Bad tenant_id: " + request.getTenantId();

            if (!validIp) {
                message += ", ";
            }
        }
        if (!validIp) {
            message += "Bad management_ip: " + request.getManagementIp();
        }
        return message;
    }
}

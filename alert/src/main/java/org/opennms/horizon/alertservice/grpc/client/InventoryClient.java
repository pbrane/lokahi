package org.opennms.horizon.alertservice.grpc.client;

import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import lombok.RequiredArgsConstructor;
import org.opennms.horizon.inventory.dto.ListTagsByEntityIdParamsDTO;
import org.opennms.horizon.inventory.dto.TagDTO;
import org.opennms.horizon.inventory.dto.TagEntityIdDTO;
import org.opennms.horizon.inventory.dto.TagListDTO;
import org.opennms.horizon.inventory.dto.TagServiceGrpc;
import org.opennms.horizon.shared.constants.GrpcConstants;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class InventoryClient {
    private final ManagedChannel channel;
    private final long deadline;

    private TagServiceGrpc.TagServiceBlockingStub tagStub;

    protected void initialStubs() {
        tagStub = TagServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }

    public List<String> getTagsByNodeId(String tenantId, long nodeId) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_BYPASS_KEY, String.valueOf(true));
        metadata.put(GrpcConstants.TENANT_ID_BYPASS_KEY, tenantId);

        ListTagsByEntityIdParamsDTO requestDto = ListTagsByEntityIdParamsDTO.newBuilder()
                .setEntityId(TagEntityIdDTO.newBuilder().setNodeId(nodeId))
                .build();

        return tagStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
            .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
            .getTagsByEntityId(requestDto).getTagsList().stream().map(TagDTO::getName).toList();
    }
}

/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.horizon.server.service.grpc;

import com.google.protobuf.Empty;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.opennms.horizon.inventory.discovery.IcmpActiveDiscoveryCreateDTO;
import org.opennms.horizon.inventory.discovery.IcmpActiveDiscoveryDTO;
import org.opennms.horizon.inventory.discovery.IcmpActiveDiscoveryServiceGrpc;
import org.opennms.horizon.inventory.dto.ActiveDiscoveryDTO;
import org.opennms.horizon.inventory.dto.ActiveDiscoveryList;
import org.opennms.horizon.inventory.dto.ActiveDiscoveryServiceGrpc;
import org.opennms.horizon.inventory.dto.AzureActiveDiscoveryCreateDTO;
import org.opennms.horizon.inventory.dto.AzureActiveDiscoveryDTO;
import org.opennms.horizon.inventory.dto.AzureActiveDiscoveryServiceGrpc;
import org.opennms.horizon.inventory.dto.IdList;
import org.opennms.horizon.inventory.dto.IpInterfaceDTO;
import org.opennms.horizon.inventory.dto.ListAllTagsParamsDTO;
import org.opennms.horizon.inventory.dto.ListTagsByEntityIdParamsDTO;
import org.opennms.horizon.inventory.dto.MonitorEntityResponse;
import org.opennms.horizon.inventory.dto.MonitoredEntityServiceGrpc;
import org.opennms.horizon.inventory.dto.MonitoredEntityStateDTO;
import org.opennms.horizon.inventory.dto.MonitoredEntityStatusServiceGrpc;
import org.opennms.horizon.inventory.dto.MonitoredServiceDTO;
import org.opennms.horizon.inventory.dto.MonitoredServiceGrpc;
import org.opennms.horizon.inventory.dto.MonitoredServiceQuery;
import org.opennms.horizon.inventory.dto.MonitoredState;
import org.opennms.horizon.inventory.dto.MonitoredStateQuery;
import org.opennms.horizon.inventory.dto.MonitoringLocationCreateDTO;
import org.opennms.horizon.inventory.dto.MonitoringLocationDTO;
import org.opennms.horizon.inventory.dto.MonitoringLocationServiceGrpc;
import org.opennms.horizon.inventory.dto.MonitoringSystemDTO;
import org.opennms.horizon.inventory.dto.MonitoringSystemServiceGrpc;
import org.opennms.horizon.inventory.dto.NodeCreateDTO;
import org.opennms.horizon.inventory.dto.NodeDTO;
import org.opennms.horizon.inventory.dto.NodeIdList;
import org.opennms.horizon.inventory.dto.NodeLabelSearchQuery;
import org.opennms.horizon.inventory.dto.NodeSearchResponseDTO;
import org.opennms.horizon.inventory.dto.NodeServiceGrpc;
import org.opennms.horizon.inventory.dto.NodeUpdateDTO;
import org.opennms.horizon.inventory.dto.NodesSearchBy;
import org.opennms.horizon.inventory.dto.PassiveDiscoveryDTO;
import org.opennms.horizon.inventory.dto.PassiveDiscoveryListDTO;
import org.opennms.horizon.inventory.dto.PassiveDiscoveryServiceGrpc;
import org.opennms.horizon.inventory.dto.PassiveDiscoveryToggleDTO;
import org.opennms.horizon.inventory.dto.PassiveDiscoveryUpsertDTO;
import org.opennms.horizon.inventory.dto.SearchBy;
import org.opennms.horizon.inventory.dto.SearchIpInterfaceQuery;
import org.opennms.horizon.inventory.dto.SearchQuery;
import org.opennms.horizon.inventory.dto.SimpleMonitoredEntityRequest;
import org.opennms.horizon.inventory.dto.SimpleMonitoredEntityResponse;
import org.opennms.horizon.inventory.dto.SimpleMonitoredEntityServiceGrpc;
import org.opennms.horizon.inventory.dto.SnmpInterfaceDTO;
import org.opennms.horizon.inventory.dto.TagCreateListDTO;
import org.opennms.horizon.inventory.dto.TagEntityIdDTO;
import org.opennms.horizon.inventory.dto.TagListDTO;
import org.opennms.horizon.inventory.dto.TagListParamsDTO;
import org.opennms.horizon.inventory.dto.TagNameQuery;
import org.opennms.horizon.inventory.dto.TagRemoveListDTO;
import org.opennms.horizon.inventory.dto.TagServiceGrpc;
import org.opennms.horizon.server.config.DataLoaderFactory;
import org.opennms.horizon.server.model.inventory.MonitoredServiceStatusRequest;
import org.opennms.horizon.shared.constants.GrpcConstants;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class InventoryClient {
    private final ManagedChannel channel;
    private final long deadline;
    private MonitoringLocationServiceGrpc.MonitoringLocationServiceBlockingStub locationStub;
    private NodeServiceGrpc.NodeServiceBlockingStub nodeStub;
    private MonitoringSystemServiceGrpc.MonitoringSystemServiceBlockingStub systemStub;
    private TagServiceGrpc.TagServiceBlockingStub tagStub;
    private ActiveDiscoveryServiceGrpc.ActiveDiscoveryServiceBlockingStub activeDiscoveryServiceBlockingStub;
    private IcmpActiveDiscoveryServiceGrpc.IcmpActiveDiscoveryServiceBlockingStub
            icmpActiveDiscoveryServiceBlockingStub;
    private AzureActiveDiscoveryServiceGrpc.AzureActiveDiscoveryServiceBlockingStub
            azureActiveDiscoveryServiceBlockingStub;
    private PassiveDiscoveryServiceGrpc.PassiveDiscoveryServiceBlockingStub passiveDiscoveryServiceBlockingStub;
    private MonitoredEntityServiceGrpc.MonitoredEntityServiceBlockingStub monitoredEntityServiceBlockingStub;
    private MonitoredEntityStatusServiceGrpc.MonitoredEntityStatusServiceBlockingStub
            monitoredEntityStatusServiceBlockingStub;
    private SimpleMonitoredEntityServiceGrpc.SimpleMonitoredEntityServiceBlockingStub
            simpleMonitoredEntityServiceBlockingStub;
    private MonitoredServiceGrpc.MonitoredServiceBlockingStub monitoredServiceBlockingStub;

    protected void initialStubs() {
        locationStub = MonitoringLocationServiceGrpc.newBlockingStub(channel);
        nodeStub = NodeServiceGrpc.newBlockingStub(channel);
        systemStub = MonitoringSystemServiceGrpc.newBlockingStub(channel);

        tagStub = TagServiceGrpc.newBlockingStub(channel);
        activeDiscoveryServiceBlockingStub = ActiveDiscoveryServiceGrpc.newBlockingStub(channel);
        icmpActiveDiscoveryServiceBlockingStub = IcmpActiveDiscoveryServiceGrpc.newBlockingStub(channel);
        azureActiveDiscoveryServiceBlockingStub = AzureActiveDiscoveryServiceGrpc.newBlockingStub(channel);
        passiveDiscoveryServiceBlockingStub = PassiveDiscoveryServiceGrpc.newBlockingStub(channel);
        monitoredEntityServiceBlockingStub = MonitoredEntityServiceGrpc.newBlockingStub(channel);
        monitoredEntityStatusServiceBlockingStub = MonitoredEntityStatusServiceGrpc.newBlockingStub(channel);
        simpleMonitoredEntityServiceBlockingStub = SimpleMonitoredEntityServiceGrpc.newBlockingStub(channel);
        monitoredServiceBlockingStub = MonitoredServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }

    public MonitoredServiceDTO getMonitoredService(MonitoredServiceQuery request, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return monitoredServiceBlockingStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .getMonitoredService(request);
    }

    public List<ActiveDiscoveryDTO> listActiveDiscoveries(String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return activeDiscoveryServiceBlockingStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .listDiscoveries(Empty.getDefaultInstance())
                .getActiveDiscoveriesList();
    }

    public IcmpActiveDiscoveryDTO createIcmpActiveDiscovery(IcmpActiveDiscoveryCreateDTO request, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return icmpActiveDiscoveryServiceBlockingStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .createDiscovery(request);
    }

    public List<IcmpActiveDiscoveryDTO> listIcmpDiscoveries(String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return icmpActiveDiscoveryServiceBlockingStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .listDiscoveries(Empty.getDefaultInstance())
                .getDiscoveriesList();
    }

    public IcmpActiveDiscoveryDTO upsertIcmpActiveDiscovery(IcmpActiveDiscoveryCreateDTO request, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return icmpActiveDiscoveryServiceBlockingStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .upsertActiveDiscovery(request);
    }

    public Boolean deleteIcmpActiveDiscovery(long discoveryId, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        var result = icmpActiveDiscoveryServiceBlockingStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .deleteActiveDiscovery(Int64Value.of(discoveryId));
        return result.getValue();
    }

    public IcmpActiveDiscoveryDTO getIcmpDiscoveryById(Long id, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return icmpActiveDiscoveryServiceBlockingStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .getDiscoveryById(Int64Value.of(id));
    }

    public NodeDTO createNewNode(NodeCreateDTO node, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return nodeStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .createNode(node);
    }

    public Long updateNode(NodeUpdateDTO node, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return nodeStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .updateNode(node)
                .getValue();
    }

    public List<NodeDTO> listNodes(String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return nodeStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .listNodes(Empty.newBuilder().build())
                .getNodesList();
    }

    public List<NodeDTO> listNodesByMonitoredState(String monitoredState, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        MonitoredStateQuery query = MonitoredStateQuery.newBuilder()
                .setMonitoredState(MonitoredState.valueOf(monitoredState))
                .build();
        return nodeStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .listNodesByMonitoredState(query)
                .getNodesList();
    }

    public List<NodeDTO> listNodesByNodeLabelSearch(String labelSearchTerm, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        NodeLabelSearchQuery query =
                NodeLabelSearchQuery.newBuilder().setSearchTerm(labelSearchTerm).build();
        return nodeStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .listNodesByNodeLabel(query)
                .getNodesList();
    }

    public List<NodeDTO> listNodesByTags(List<String> tags, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        TagNameQuery query = TagNameQuery.newBuilder().addAllTags(tags).build();
        return nodeStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .listNodesByTags(query)
                .getNodesList();
    }

    public NodeDTO getNodeById(long id, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return nodeStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .getNodeById(Int64Value.of(id));
    }

    public ActiveDiscoveryList getDiscoveriesByNode(long id, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return nodeStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .getDiscoveriesByNode(Int64Value.of(id));
    }

    public List<MonitoringLocationDTO> listLocations(String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return locationStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .listLocations(Empty.newBuilder().build())
                .getLocationsList();
    }

    public MonitoringLocationDTO getLocationById(long id, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return locationStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .getLocationById(Int64Value.of(id));
    }

    public MonitoringLocationDTO getLocationByName(String locationName, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return locationStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .getLocationByName(StringValue.of(locationName));
    }

    public List<MonitoringSystemDTO> listMonitoringSystems(String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return systemStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .listMonitoringSystem(Empty.newBuilder().build())
                .getSystemsList();
    }

    public List<MonitoringSystemDTO> getMonitoringSystemsByLocationId(long locationId, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return systemStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .listMonitoringSystemByLocationId(Int64Value.of(locationId))
                .getSystemsList();
    }

    public MonitoringSystemDTO getSystemBySystemId(long systemId, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return systemStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .getMonitoringSystemById(Int64Value.of(systemId));
    }

    public List<MonitoringLocationDTO> listLocationsByIds(List<DataLoaderFactory.Key> keys) {
        return keys.stream()
                .map(DataLoaderFactory.Key::getToken)
                .findFirst()
                .map(accessToken -> {
                    Metadata metadata = new Metadata();
                    metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
                    List<Int64Value> idValues =
                            keys.stream().map(k -> Int64Value.of(k.getId())).toList();
                    return locationStub
                            .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                            .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                            .listLocationsByIds(
                                    IdList.newBuilder().addAllIds(idValues).build())
                            .getLocationsList();
                })
                .orElseThrow();
    }

    public List<MonitoringLocationDTO> searchLocations(String searchTerm, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return locationStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .searchLocations(StringValue.of(searchTerm))
                .getLocationsList();
    }

    public boolean deleteNode(long nodeId, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return nodeStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .deleteNode(Int64Value.of(nodeId))
                .getValue();
    }

    public boolean deleteMonitoringSystem(long systemId, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return systemStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .deleteMonitoringSystem(
                        Int64Value.newBuilder().setValue(systemId).build())
                .getValue();
    }

    public boolean startScanByNodeIds(List<Long> ids, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return nodeStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .startNodeScanByIds(NodeIdList.newBuilder().addAllIds(ids).build())
                .getValue();
    }

    public AzureActiveDiscoveryDTO createAzureActiveDiscovery(
            AzureActiveDiscoveryCreateDTO request, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return azureActiveDiscoveryServiceBlockingStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .createDiscovery(request);
    }

    public PassiveDiscoveryDTO upsertPassiveDiscovery(PassiveDiscoveryUpsertDTO passiveDiscovery, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return passiveDiscoveryServiceBlockingStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .upsertDiscovery(passiveDiscovery);
    }

    public Boolean deletePassiveDiscovery(long id, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        var result = passiveDiscoveryServiceBlockingStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .deleteDiscovery(Int64Value.of(id));
        return result.getValue();
    }

    public PassiveDiscoveryListDTO listPassiveDiscoveries(String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return passiveDiscoveryServiceBlockingStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .listAllDiscoveries(Empty.getDefaultInstance());
    }

    public TagListDTO addTags(TagCreateListDTO tagCreateList, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return tagStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .addTags(tagCreateList);
    }

    public void removeTags(TagRemoveListDTO tagRemoveList, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        tagStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .removeTags(tagRemoveList);
    }

    public TagListDTO getTagsByNodeId(long nodeId, String searchTerm, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        ListTagsByEntityIdParamsDTO params = ListTagsByEntityIdParamsDTO.newBuilder()
                .setEntityId(TagEntityIdDTO.newBuilder().setNodeId(nodeId))
                .setParams(buildTagListParams(searchTerm))
                .build();
        return tagStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .getTagsByEntityId(params);
    }

    public TagListDTO getTagsByNodeId(long nodeId, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        ListTagsByEntityIdParamsDTO params = ListTagsByEntityIdParamsDTO.newBuilder()
                .setEntityId(TagEntityIdDTO.newBuilder().setNodeId(nodeId))
                .build();
        return tagStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .getTagsByEntityId(params);
    }

    public TagListDTO getTagsByActiveDiscoveryId(long activeDiscoveryId, String searchTerm, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        ListTagsByEntityIdParamsDTO params = ListTagsByEntityIdParamsDTO.newBuilder()
                .setEntityId(TagEntityIdDTO.newBuilder().setActiveDiscoveryId(activeDiscoveryId))
                .setParams(buildTagListParams(searchTerm))
                .build();
        return tagStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .getTagsByEntityId(params);
    }

    public TagListDTO getTagsByPassiveDiscoveryId(long passiveDiscoveryId, String searchTerm, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        ListTagsByEntityIdParamsDTO params = ListTagsByEntityIdParamsDTO.newBuilder()
                .setEntityId(TagEntityIdDTO.newBuilder().setPassiveDiscoveryId(passiveDiscoveryId))
                .setParams(buildTagListParams(searchTerm))
                .build();
        return tagStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .getTagsByEntityId(params);
    }

    public TagListDTO getTags(String searchTerm, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        ListAllTagsParamsDTO params = ListAllTagsParamsDTO.newBuilder()
                .setParams(buildTagListParams(searchTerm))
                .build();
        return tagStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .getTags(params);
    }

    private TagListParamsDTO buildTagListParams(String searchTerm) {
        TagListParamsDTO.Builder paramBuilder = TagListParamsDTO.newBuilder();
        if (StringUtils.hasText(searchTerm)) {
            paramBuilder.setSearchTerm(searchTerm);
        }
        return paramBuilder.build();
    }

    public PassiveDiscoveryDTO createPassiveDiscoveryToggle(PassiveDiscoveryToggleDTO request, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return passiveDiscoveryServiceBlockingStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .toggleDiscovery(request);
    }

    public IpInterfaceDTO getIpInterfaceById(long id, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return nodeStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .getIpInterfaceById(Int64Value.of(id));
    }

    public MonitoringLocationDTO createLocation(MonitoringLocationCreateDTO location, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return locationStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .createLocation(location);
    }

    public MonitoringLocationDTO updateLocation(MonitoringLocationDTO location, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return locationStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .updateLocation(location);
    }

    public boolean deleteLocation(long id, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return locationStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .deleteLocation(Int64Value.of(id))
                .getValue();
    }

    public MonitoredEntityStateDTO getMonitoredEntityState(MonitoredServiceStatusRequest request, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return monitoredEntityStatusServiceBlockingStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .getMonitoredEntityState(StringValue.of(request.getMonitoredEntityId()));
    }

    public List<MonitorEntityResponse> getAllMonitoredEntitiesByLocation(long locationId, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return monitoredEntityServiceBlockingStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .getAllMonitoredEntitiesByLocation(Int64Value.of(locationId))
                .getListMonitorEntityResponseList();
    }

    public List<MonitorEntityResponse> getAllMonitoredEntitiesBySearchTerm(
            long locationId, String providerId, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);

        SearchQuery query = SearchQuery.newBuilder()
                .setLocationId(locationId)
                .setProviderId(providerId)
                .build();

        return monitoredEntityServiceBlockingStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .getAllMonitoredEntitiesBySearchTerm(query)
                .getListMonitorEntityResponseList();
    }

    public List<MonitorEntityResponse> getAllMonitoredEntities(String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return monitoredEntityServiceBlockingStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .getAllMonitoredEntities(Empty.getDefaultInstance())
                .getListMonitorEntityResponseList();
    }

    public long getNodeCount(String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return nodeStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .getNodeCount(Empty.newBuilder().build())
                .getValue();
    }

    public List<IpInterfaceDTO> searchIpInterfacesByNodeAndSearchTerm(
            Long nodeId, String ipInterfaceSearchTerm, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        SearchIpInterfaceQuery query = SearchIpInterfaceQuery.newBuilder()
                .setNodeId(nodeId)
                .setSearchTerm(ipInterfaceSearchTerm)
                .build();
        return nodeStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .searchIpInterfaces(query)
                .getIpInterfaceList();
    }

    public List<SnmpInterfaceDTO> listSnmpInterfaces(String searchTerm, Long nodeId, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        SearchBy query = SearchBy.newBuilder()
                .setSearchTerm(searchTerm)
                .setNodeId(nodeId)
                .build();
        return nodeStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .listSnmpInterfaces(query)
                .getSnmpInterfacesList();
    }

    public List<Integer> getMonitoringPoliciesByNode(Long nodeId, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return nodeStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .getMonitoringPoliciesByNode(Int64Value.of(nodeId))
                .getIdsList()
                .stream()
                .map(Long::intValue)
                .collect(Collectors.toList());
    }

    public NodeSearchResponseDTO searchNodes(
            String searchValue,
            String searchType,
            Integer pageSize,
            int page,
            String sortBy,
            boolean sortAscending,
            String accessToken) {
        int pageSizeValue = pageSize != null ? pageSize : 0;
        var nodeSearchByBuilder = NodesSearchBy.newBuilder()
                .setPageSize(pageSizeValue)
                .setPage(page)
                .setSortAscending(sortAscending);

        if (Strings.isNotBlank(searchType)) {
            nodeSearchByBuilder.setSearchType(searchType);
        }
        if (Strings.isNotBlank(searchValue)) {
            nodeSearchByBuilder.setSearchValue(searchValue);
        }
        if (Strings.isNotBlank(sortBy)) {
            nodeSearchByBuilder.setSortBy(sortBy);
        }
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return nodeStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .searchNodes(nodeSearchByBuilder.build());
    }

    public List<SimpleMonitoredEntityResponse> getAllSimpleMonitoredEntities(String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return this.simpleMonitoredEntityServiceBlockingStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .list(Empty.getDefaultInstance())
                .getEntryList();
    }

    public SimpleMonitoredEntityResponse upsertSimpleMonitoredEntity(
            SimpleMonitoredEntityRequest entity, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return this.simpleMonitoredEntityServiceBlockingStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .upsert(entity);
    }

    public boolean deleteSimpleMonitoredEntity(String id, String accessToken) {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, accessToken);
        return this.simpleMonitoredEntityServiceBlockingStub
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
                .delete(StringValue.of(id))
                .getValue();
    }
}

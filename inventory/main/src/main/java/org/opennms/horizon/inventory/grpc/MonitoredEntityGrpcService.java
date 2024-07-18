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
package org.opennms.horizon.inventory.grpc;

import com.google.protobuf.Empty;
import com.google.protobuf.Int64Value;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.opennms.horizon.inventory.dto.ListMonitorEntities;
import org.opennms.horizon.inventory.dto.MonitorEntityResponse;
import org.opennms.horizon.inventory.dto.MonitoredEntityServiceGrpc;
import org.opennms.horizon.inventory.dto.SearchQuery;
import org.opennms.horizon.inventory.mapper.MonitorEntityMapper;
import org.opennms.horizon.inventory.monitoring.MonitoredEntityService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MonitoredEntityGrpcService extends MonitoredEntityServiceGrpc.MonitoredEntityServiceImplBase {

    private final TenantLookup tenantLookup;
    private final MonitoredEntityService monitoredEntityService;
    private final MonitorEntityMapper mapper;

    @Override
    public void getAllMonitoredEntitiesByLocation(
            Int64Value request, StreamObserver<ListMonitorEntities> responseObserver) {
        String tenantId = tenantLookup.lookupTenantId(Context.current()).orElseThrow();
        List<MonitorEntityResponse> list =
                monitoredEntityService.getAllMonitoredEntities(tenantId, request.getValue()).stream()
                        .map(mapper::modelToDTO)
                        .toList();
        responseObserver.onNext(ListMonitorEntities.newBuilder()
                .addAllListMonitorEntityResponse(list)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getAllMonitoredEntitiesBySearchTerm(
            SearchQuery request, StreamObserver<ListMonitorEntities> responseObserver) {
        String tenantId = tenantLookup.lookupTenantId(Context.current()).orElseThrow();
        List<MonitorEntityResponse> list =
                monitoredEntityService.getAllMonitoredEntities(tenantId, request.getLocationId()).stream()
                        .filter(s -> s.getSource().getProviderId().equals(request.getProviderId()))
                        .map(mapper::modelToDTO)
                        .toList();

        responseObserver.onNext(ListMonitorEntities.newBuilder()
                .addAllListMonitorEntityResponse(list)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getAllMonitoredEntities(Empty request, StreamObserver<ListMonitorEntities> responseObserver) {
        String tenantId = tenantLookup.lookupTenantId(Context.current()).orElseThrow();

        final var entities = ListMonitorEntities.newBuilder();

        this.monitoredEntityService.getAllMonitoredEntities(tenantId).stream()
                .map(mapper::modelToDTO)
                .forEach(entities::addListMonitorEntityResponse);

        responseObserver.onNext(entities.build());
        responseObserver.onCompleted();
    }
}

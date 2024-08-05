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
package org.opennms.horizon.inventory.monitoring.simple;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import com.google.rpc.Code;
import com.google.rpc.Status;
import io.grpc.Context;
import io.grpc.protobuf.StatusProto;
import io.grpc.stub.StreamObserver;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opennms.horizon.inventory.dto.SimpleMonitoredEntityRequest;
import org.opennms.horizon.inventory.dto.SimpleMonitoredEntityResponse;
import org.opennms.horizon.inventory.dto.SimpleMonitoredEntityResponseList;
import org.opennms.horizon.inventory.dto.SimpleMonitoredEntityServiceGrpc;
import org.opennms.horizon.inventory.grpc.TenantLookup;
import org.opennms.horizon.inventory.monitoring.MonitoredEntityService;
import org.opennms.horizon.inventory.monitoring.simple.config.SimpleMonitorDiscoveryService;
import org.opennms.horizon.inventory.repository.SimpleMonitoredEntityRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimpleMonitoredEntityGrpcService
        extends SimpleMonitoredEntityServiceGrpc.SimpleMonitoredEntityServiceImplBase {

    private final SimpleMonitoredEntityRepository repository;
    private final SimpleMonitoredEntityMapper mapper;
    private final TenantLookup tenantLookup;

    private final MonitoredEntityService monitoredEntityService;
    private final SimpleMonitorDiscoveryService simpleMonitorDiscoveryService;

    @Override
    public void list(final Empty request, final StreamObserver<SimpleMonitoredEntityResponseList> responseObserver) {
        final var entities = this.tenantLookup
                .lookupTenantId(Context.current())
                .map(this.repository::findByTenantId)
                .orElseThrow();

        responseObserver.onNext(this.mapper.map(entities));
        responseObserver.onCompleted();
    }

    @Override
    public void upsert(
            final SimpleMonitoredEntityRequest request,
            final StreamObserver<SimpleMonitoredEntityResponse> responseObserver) {

        var tenantId = this.tenantLookup.lookupTenantId(Context.current()).orElseThrow();

        if (!tenantId.isEmpty()) {

            final SimpleMonitoredEntityResponse response;

            SimpleMonitoredActiveDiscovery sme;

            sme = this.mapper.map(tenantId, request);
            sme.setTenantId(tenantId);
            sme.setCreateTime(LocalDateTime.now());
            sme.setName(request.getName());

            response = simpleMonitorDiscoveryService.createActiveDiscovery(sme, tenantId);
            responseObserver.onNext(response);
            responseObserver.onCompleted();

            this.monitoredEntityService.publishTaskSet(sme.getTenantId(), sme.getLocationId());
        } else {
            responseObserver.onError(StatusProto.toStatusRuntimeException(createMissingTenant()));
        }
    }

    @Override
    @Transactional
    public void delete(final StringValue request, final StreamObserver<BoolValue> responseObserver) {
        final var tenantId = this.tenantLookup.lookupTenantId(Context.current()).orElseThrow();

        final var id = UUID.fromString(request.getValue());

        final var sme = this.repository.getByTenantIdAndId(tenantId, id);
        if (sme == null) {
            responseObserver.onNext(BoolValue.newBuilder().setValue(false).build());
            responseObserver.onCompleted();
            return;
        }

        this.repository.delete(sme);

        responseObserver.onNext(BoolValue.newBuilder().setValue(true).build());
        responseObserver.onCompleted();

        this.monitoredEntityService.publishTaskSet(sme.getTenantId(), sme.getLocationId());
    }

    private Status createMissingTenant() {
        return Status.newBuilder()
                .setCode(Code.INVALID_ARGUMENT_VALUE)
                .setMessage("Missing tenantId")
                .build();
    }
}

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
import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opennms.horizon.inventory.dto.SimpleMonitoredEntityRequest;
import org.opennms.horizon.inventory.dto.SimpleMonitoredEntityResponse;
import org.opennms.horizon.inventory.dto.SimpleMonitoredEntityResponseList;
import org.opennms.horizon.inventory.dto.SimpleMonitoredEntityServiceGrpc;
import org.opennms.horizon.inventory.grpc.TenantLookup;
import org.opennms.horizon.inventory.monitoring.MonitoredEntityService;
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
        final var tenantId = this.tenantLookup.lookupTenantId(Context.current()).orElseThrow();

        final var sme = this.mapper.map(tenantId, request);
        final var response = this.mapper.map(this.repository.save(sme));

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        this.monitoredEntityService.publishTaskSet(sme.getTenantId(), sme.getLocationId());
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
}

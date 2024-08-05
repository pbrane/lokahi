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
import com.google.protobuf.StringValue;
import com.google.rpc.Code;
import com.google.rpc.Status;
import io.grpc.Context;
import io.grpc.protobuf.StatusProto;
import io.grpc.stub.StreamObserver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.opennms.horizon.inventory.dto.ListMonitoredEntityStateDTO;
import org.opennms.horizon.inventory.dto.MonitoredEntityStateDTO;
import org.opennms.horizon.inventory.dto.MonitoredEntityStatusServiceGrpc;
import org.opennms.horizon.inventory.service.MonitoredEntityStateService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MonitoredEntityStatusGrpcService
        extends MonitoredEntityStatusServiceGrpc.MonitoredEntityStatusServiceImplBase {

    private final MonitoredEntityStateService monitorStatusService;
    private final TenantLookup tenantLookup;

    @Override
    public void getMonitoredEntityState(StringValue request, StreamObserver<MonitoredEntityStateDTO> responseObserver) {
        String tenantId = tenantLookup.lookupTenantId(Context.current()).orElseThrow();
        var optional = monitorStatusService.getMonitoredEntityState(tenantId, request.getValue());
        if (optional.isPresent()) {
            var monitorStatus = optional.get();
            responseObserver.onNext(monitorStatus);
            responseObserver.onCompleted();
        } else {
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("Invalid monitor service id")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
        }
    }

    @Override
    public void listAllMonitors(Empty request, StreamObserver<ListMonitoredEntityStateDTO> responseObserver) {
        final var tenantId = this.tenantLookup.lookupTenantId(Context.current()).orElseThrow();
        List<MonitoredEntityStateDTO> monitoredEntityStateDTOS = monitorStatusService.listAllSimpleMonitor(tenantId);
        ListMonitoredEntityStateDTO builder = ListMonitoredEntityStateDTO.newBuilder()
                .addAllEntry(monitoredEntityStateDTOS)
                .build();
        responseObserver.onNext(builder);
        responseObserver.onCompleted();
    }
}

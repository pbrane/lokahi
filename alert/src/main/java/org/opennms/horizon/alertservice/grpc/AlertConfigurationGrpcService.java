/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.horizon.alertservice.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.opennms.horizon.alerts.proto.Alert;
import org.opennms.horizon.alerts.proto.AlertConfigurationServiceGrpc;
import org.opennms.horizon.alerts.proto.AlertDefinition;
import org.opennms.horizon.alerts.proto.ListAlertDefinitionsResponse;
import org.opennms.horizon.alerts.proto.ListAlertsResponse;
import org.opennms.horizon.alertservice.db.repository.AlertDefinitionRepository;
import org.opennms.horizon.alertservice.db.repository.AlertRepository;
import org.opennms.horizon.alertservice.service.AlertMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * A temporary noop implementation of the service.
 *
 * Will evolve with the data model as necessary.
 */
@Component
@RequiredArgsConstructor
public class AlertConfigurationGrpcService extends AlertConfigurationServiceGrpc.AlertConfigurationServiceImplBase {


    private final AlertDefinitionRepository alertDefinitionRepository;

    private final AlertMapper alertMapper;


    /*
  rpc listAlertDefinitions(ListAlertDefinitionsRequest) returns (ListAlertDefinitionsResponse) {};
  rpc getAlertDefinition(google.protobuf.UInt64Value) returns (AlertDefinition) {}

  rpc insertAlertDefinition(AlertDefinition) returns (AlertDefinition) {}
  rpc updateAlertDefinition(AlertDefinition) returns (AlertDefinition) {}
  rpc removeAlertDefinition(google.protobuf.UInt64Value) returns (google.protobuf.BoolValue) {}
     */

    @Override
    public void listAlertDefinitions(org.opennms.horizon.alerts.proto.ListAlertDefinitionsRequest request,
                                     io.grpc.stub.StreamObserver<org.opennms.horizon.alerts.proto.ListAlertDefinitionsResponse> responseObserver) {
        List<AlertDefinition> alertDefinition = alertDefinitionRepository.findAll().stream()
            .map(alertMapper::alertDefinitionToProto)
            .toList();

        ListAlertDefinitionsResponse response = ListAlertDefinitionsResponse.newBuilder()
            .addAllDefinitions(alertDefinition)
            .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}

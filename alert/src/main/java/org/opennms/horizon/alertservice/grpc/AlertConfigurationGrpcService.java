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

import com.google.rpc.Code;
import com.google.rpc.Status;
import io.grpc.protobuf.StatusProto;
import lombok.RequiredArgsConstructor;
import org.opennms.horizon.alerts.proto.AlertConfigurationServiceGrpc;
import org.opennms.horizon.alerts.proto.AlertDefinition;
import org.opennms.horizon.alerts.proto.EventMatch;
import org.opennms.horizon.alerts.proto.ListAlertDefinitionsResponse;
import org.opennms.horizon.alertservice.db.repository.AlertDefinitionRepository;
import org.opennms.horizon.alertservice.service.AlertDefinitionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AlertConfigurationGrpcService extends AlertConfigurationServiceGrpc.AlertConfigurationServiceImplBase {

    private static final Logger LOG = LoggerFactory.getLogger(AlertConfigurationGrpcService.class);

    private final AlertDefinitionRepository alertDefinitionRepository;

    private final AlertDefinitionMapper alertMapper;

    @Override
    public void listAlertDefinitions(org.opennms.horizon.alerts.proto.ListAlertDefinitionsRequest request,
                                     io.grpc.stub.StreamObserver<org.opennms.horizon.alerts.proto.ListAlertDefinitionsResponse> responseObserver) {
        List<AlertDefinition> alertDefinitions = alertDefinitionRepository.findAll().stream()
            .map(alertMapper::toProto)
            .toList();

        ListAlertDefinitionsResponse response = ListAlertDefinitionsResponse.newBuilder()
            .addAllDefinitions(alertDefinitions)
            .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getAlertDefinition(com.google.protobuf.UInt64Value request,
                                   io.grpc.stub.StreamObserver<org.opennms.horizon.alerts.proto.AlertDefinition> responseObserver) {
        if (!alertDefinitionRepository.existsById(request.getValue())){
            LOG.error("Alert definition null id="+request.getValue());
            responseObserver.onError(StatusProto.toStatusRuntimeException(Status.newBuilder()
                .setCode(Code.INVALID_ARGUMENT_VALUE)
                .setMessage("Can't find AlertDefinition for id="+request.getValue())
                .build()));
            return;
        }
        org.opennms.horizon.alertservice.db.entity.AlertDefinition alertDefinition = alertDefinitionRepository.getReferenceById(request.getValue());
        org.opennms.horizon.alerts.proto.AlertDefinition response = org.opennms.horizon.alerts.proto.AlertDefinition.newBuilder()
            .setUei(alertDefinition.getUei())
            .addAllMatch(getMatchesAsProto(alertDefinition))
            .setReductionKey(alertDefinition.getReductionKey())
            .setClearKey(alertDefinition.getClearKey())
            .setType(alertDefinition.getType())
            .setManagedObjectType(alertDefinition.getManagedObjectType())
            .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void insertAlertDefinition(org.opennms.horizon.alerts.proto.AlertDefinition request,
              io.grpc.stub.StreamObserver<org.opennms.horizon.alerts.proto.AlertDefinition> responseObserver){

    }

    public void updateAlertDefinition(org.opennms.horizon.alerts.proto.AlertDefinition request,
        io.grpc.stub.StreamObserver<org.opennms.horizon.alerts.proto.AlertDefinition> responseObserver){

    }
    public void removeAlertDefinition(com.google.protobuf.UInt64Value request,
                                      io.grpc.stub.StreamObserver<com.google.protobuf.BoolValue> responseObserver){

    }

    List<org.opennms.horizon.alerts.proto.EventMatch> getMatchesAsProto(org.opennms.horizon.alertservice.db.entity.AlertDefinition alertDefinition){
        List<org.opennms.horizon.alerts.proto.EventMatch> matches = new ArrayList<org.opennms.horizon.alerts.proto.EventMatch>(alertDefinition.getMatch().size());
        for (org.opennms.horizon.alertservice.db.entity.EventMatch a:  alertDefinition.getMatch()) {
            EventMatch match = org.opennms.horizon.alerts.proto.EventMatch.newBuilder()
                .setName(a.getName())
                .setValue(a.getValue())
                .build();
            matches.add(match);
        }
        return matches;
    }
}

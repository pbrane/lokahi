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

package org.opennms.horizon.alarmservice.grpc;

import com.google.protobuf.BoolValue;
import com.google.protobuf.UInt64Value;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.opennms.horizon.alarms.proto.Alarm;
import org.opennms.horizon.alarms.proto.AlarmServiceGrpc;
import org.opennms.horizon.alarms.proto.ListAlarmsRequest;
import org.opennms.horizon.alarms.proto.ListAlarmsResponse;
import org.opennms.horizon.alarmservice.api.AlarmService;
import org.opennms.horizon.alarmservice.db.repository.AlarmRepository;
import org.opennms.horizon.alarmservice.service.AlarmMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AlarmGrpcService extends AlarmServiceGrpc.AlarmServiceImplBase {
    private final AlarmMapper alarmMapper;
    private final AlarmRepository alarmRepository;
    private final AlarmService alarmService;

    @Override
    public void listAlarms(ListAlarmsRequest request, StreamObserver<ListAlarmsResponse> responseObserver) {
        List<Alarm> alarms = alarmRepository.findAll().stream()
            .map(alarmMapper::toProto)
            .toList();

        ListAlarmsResponse response = ListAlarmsResponse.newBuilder()
            .addAllAlarms(alarms)
            .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteAlarm(UInt64Value request, StreamObserver<BoolValue> responseObserver) {
        alarmService.deleteAlarmById(request.getValue());
        responseObserver.onNext(BoolValue.of(true));
        responseObserver.onCompleted();
    }

    @Override
    public void acknowledgeAlarm(UInt64Value request, StreamObserver<Alarm> responseObserver) {
        responseObserver.onNext(alarmService.acknowledgeAlarmById(request.getValue()).orElse(null));
        responseObserver.onCompleted();
    }

    @Override
    public void unacknowledgeAlarm(UInt64Value request, StreamObserver<Alarm> responseObserver) {
        responseObserver.onNext(alarmService.unacknowledgeAlarmById(request.getValue()).orElse(null));
        responseObserver.onCompleted();
    }

}

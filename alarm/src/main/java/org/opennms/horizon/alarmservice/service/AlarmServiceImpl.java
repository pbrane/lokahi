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

package org.opennms.horizon.alarmservice.service;

import lombok.RequiredArgsConstructor;
import org.opennms.horizon.alarms.proto.Alarm;
import org.opennms.horizon.alarmservice.api.AlarmLifecyleListener;
import org.opennms.horizon.alarmservice.api.AlarmService;
import org.opennms.horizon.alarmservice.db.repository.AlarmRepository;
import org.opennms.horizon.events.proto.Event;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlarmServiceImpl implements AlarmService {

    private final AlarmEventProcessor alarmEventProcessor;
    private final AlarmRepository alarmRepository;
    private final AlarmListenerRegistry alarmListenerRegistry;
    private final AlarmMapper alarmMapper;

    @Override
    public Optional<Alarm> reduceEvent(Event e) {
        Optional<Alarm> alarm = alarmEventProcessor.process(e);
        if (alarm.isPresent()) {
            alarmListenerRegistry.forEachListener((l) -> l.handleNewOrUpdatedAlarm(alarm.get()));
        }
        return alarm;
    }

    @Override
    @Transactional
    public void deleteAlarmById(long id) {
        Optional<org.opennms.horizon.alarmservice.db.entity.Alarm> dbAlarm = alarmRepository.findById(id);
        if (dbAlarm.isEmpty()) {
            return;
        }

        alarmRepository.deleteById(id);
        Alarm alarm = alarmMapper.toProto(dbAlarm.get());
        alarmListenerRegistry.forEachListener((l) -> l.handleDeletedAlarm(alarm));
    }

    @Override
    @Transactional
    public void deleteAlarm(Alarm alarm) {
        alarmRepository.deleteById(alarm.getDatabaseId());
        alarmListenerRegistry.forEachListener((l) -> l.handleDeletedAlarm(alarm));
    }

    @Override
    @Transactional
    public Optional<Alarm> acknowledgeAlarmById(long id) {
        Optional<org.opennms.horizon.alarmservice.db.entity.Alarm> dbAlarm = alarmRepository.findById(id);
        if (dbAlarm.isEmpty()) {
            return Optional.empty();
        }

        org.opennms.horizon.alarmservice.db.entity.Alarm alarm = dbAlarm.get();
        alarm.setAcknowledgedAt(new Date());
        alarm.setAcknowledgedByUser("me");
        alarmRepository.save(alarm);
        return Optional.of(alarmMapper.toProto(alarm));
    }

    @Override
    @Transactional
    public Optional<Alarm> unacknowledgeAlarmById(long id) {
        Optional<org.opennms.horizon.alarmservice.db.entity.Alarm> dbAlarm = alarmRepository.findById(id);
        if (dbAlarm.isEmpty()) {
            return Optional.empty();
        }

        org.opennms.horizon.alarmservice.db.entity.Alarm alarm = dbAlarm.get();
        alarm.setAcknowledgedAt(null);
        alarm.setAcknowledgedByUser(null);
        alarmRepository.save(alarm);
        return Optional.of(alarmMapper.toProto(alarm));
    }

    @Override
    public void addListener(AlarmLifecyleListener listener) {
        alarmListenerRegistry.addListener(listener);
    }

    @Override
    public void removeListener(AlarmLifecyleListener listener) {
        alarmListenerRegistry.addListener(listener);
    }
}
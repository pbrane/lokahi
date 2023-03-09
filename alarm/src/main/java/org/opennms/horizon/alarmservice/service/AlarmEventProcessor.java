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

import com.google.common.base.Strings;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.opennms.horizon.alarms.proto.Alarm;
import org.opennms.horizon.alarms.proto.AlarmDefinition;
import org.opennms.horizon.alarms.proto.AlarmType;
import org.opennms.horizon.alarms.proto.ManagedObjectType;
import org.opennms.horizon.alarmservice.db.repository.AlarmDefinitionRepository;
import org.opennms.horizon.alarmservice.db.repository.AlarmRepository;
import org.opennms.horizon.events.proto.Event;
import org.opennms.horizon.model.common.proto.Severity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.Optional;

/**
 * Used to process/reduce events to alarms.
 */
@Service
@RequiredArgsConstructor
public class AlarmEventProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(AlarmEventProcessor.class);

    private final AlarmRepository alarmRepository;

    private final AlarmMapper alarmMapper;

    private final AlarmDefinitionRepository alarmDefinitionRepository;

    private final MeterRegistry registry;

    private Counter eventsWithoutAlarmDataCounter;

    @PostConstruct
    public void init() {
        eventsWithoutAlarmDataCounter = registry.counter("events_without_alarm_data_counter");
    }

    @Transactional
    public Optional<Alarm> process(Event e) {
        LOG.trace("Processing event with UEI: {} for tenant id: {}", e.getUei(), e.getTenantId());
        org.opennms.horizon.alarmservice.db.entity.Alarm dbAlarm = addOrReduceEventAsAlarm(e);
        if (dbAlarm == null) {
            LOG.debug("No alarm returned from processing event with UEI: {} for tenant id: {}", e.getUei(), e.getTenantId());
            return Optional.empty();
        }
        return Optional.of(alarmMapper.toProto(dbAlarm));
    }

    private AlarmData getAlarmData(Event event, AlarmDefinition alarmDefinition) {
        var reductionKey = String.format(alarmDefinition.getReductionKey(), event.getTenantId(), event.getUei(), event.getNodeId());
        String clearKey = null;
        if (!Strings.isNullOrEmpty(alarmDefinition.getClearKey())) {
            clearKey = String.format(alarmDefinition.getClearKey(), event.getTenantId(), event.getNodeId());
        }
        return new AlarmData(reductionKey, clearKey, alarmDefinition.getType());
    }

    protected org.opennms.horizon.alarmservice.db.entity.Alarm addOrReduceEventAsAlarm(Event event) {
        AlarmDefinition alarmDef = alarmDefinitionRepository.getAlarmDefinitionForEvent(event);
        if (alarmDef == null) {
            // No alarm definition matching, no alarm to create
            eventsWithoutAlarmDataCounter.increment();
            return null;
        }
        AlarmData alarmData = getAlarmData(event, alarmDef);

        org.opennms.horizon.alarmservice.db.entity.Alarm alarm = null;
        if (alarmData.clearKey() != null) {
            // If a clearKey is set, determine if there is an existing alarm, and reduce onto that one
            alarm = alarmRepository.findByReductionKey(alarmData.clearKey());
            if (alarm == null) {
                LOG.debug("No existing alarm found with clear key: {}. This is possibly an out-of-order event: {}", alarmData.clearKey(), event);
            }
        }

        if (alarm == null) {
            // If we didn't find an existing alarm to reduce to with the clearKey, the lookup by reductionKey
            alarm = alarmRepository.findByReductionKey(alarmData.reductionKey());
        }

        if (alarm == null ) {
            // No existing alarm found, create a new one
            alarm = createNewAlarm(event, alarmData);
        } else {
            // Existing alarm found, update it
            alarm.incrementCount();
            alarm.setLastEventId(event.getDatabaseId());
            alarm.setType(alarmData.type());
            if (AlarmType.CLEAR.equals(alarm.getType())) {
                // Set the severity to CLEARED when reducing alarms
                alarm.setSeverity(Severity.CLEARED);
            } else {
                alarm.setSeverity(event.getSeverity());
            }
        }

        // FIXME: If the alarm is going to be delete immediately, should we even bother creating it?
        alarmRepository.save(alarm);
        return alarm;
    }

    private org.opennms.horizon.alarmservice.db.entity.Alarm createNewAlarm(Event event, AlarmData alarmData) {
        org.opennms.horizon.alarmservice.db.entity.Alarm alarm = new org.opennms.horizon.alarmservice.db.entity.Alarm();
        alarm.setTenantId(event.getTenantId());
        alarm.setType(alarmData.type());
        alarm.setReductionKey(alarmData.reductionKey());
        alarm.setClearKey(alarmData.clearKey());
        alarm.setCounter(1L);
        if (event.getNodeId() > 0) {
            alarm.setManagedObjectType(ManagedObjectType.NODE);
            alarm.setManagedObjectInstance(Long.toString(event.getNodeId()));
        }
        // FIXME: We should be using the source time of the event and not the time at which it was produced
        alarm.setLastEventTime(new Date(event.getProducedTimeMs()));
        alarm.setSeverity(event.getSeverity());
        alarm.setEventUei(event.getUei() );
        return alarm;
    }

    private record AlarmData(String reductionKey, String clearKey, AlarmType type) { }

}

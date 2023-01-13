/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.horizon.alarmservice.api;

import java.util.Date;
import java.util.List;
import org.opennms.horizon.alarmservice.db.entity.Alarm;
import org.opennms.horizon.alarmservice.model.AlarmDTO;
import org.opennms.horizon.alarmservice.model.AlarmSeverity;
import org.opennms.horizon.events.proto.Event;

/**
 * This API is intended to provide RHS functionality for Drools Alarmd and
 * Situation rules.
 */
public interface AlarmService {

    AlarmDTO clearAlarm(Alarm alarm, Date now);

    AlarmDTO clearAlarm(Long alarmId, Date now);

    AlarmDTO deleteAlarm(Long id);

    AlarmDTO deleteAlarm(Alarm alarm);

    AlarmDTO unclearAlarm(Alarm alarm, Date now);

    AlarmDTO unclearAlarm(Long alarmId, Date now);

    AlarmDTO escalateAlarm(Alarm alarm, Date now);

    AlarmDTO escalateAlarm(Long alarmId, Date now);

    AlarmDTO acknowledgeAlarm(Alarm alarm, Date now, String userId);

    AlarmDTO acknowledgeAlarm(Long alarmId, Date now, String userId);

    AlarmDTO unAcknowledgeAlarm(Long alarmId, Date now);

    AlarmDTO unAcknowledgeAlarm(Alarm alarm, Date now);

    AlarmDTO setSeverity(Alarm alarm, AlarmSeverity severity, Date now);

    AlarmDTO setSeverity(Long alarmId, AlarmSeverity severity, Date now);

    List<AlarmDTO> getAllAlarms(String tenantId);

    AlarmDTO process(Event e);

    AlarmDTO removeStickyMemo(long alarmId);

    AlarmDTO updateStickyMemo(Long alarmId, String body);

}

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

import org.junit.Test;
import org.opennms.horizon.alarmservice.db.entity.Alarm;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class AlarmMapperTest {

    @Test
    public void canMapAlarm() {
        Alarm dbAlarm = new Alarm();
        dbAlarm.setTenantId("wow1");
        dbAlarm.setAlarmId(42L);
        dbAlarm.setCounter(3L);
        dbAlarm.setReductionKey("oops:1");
        dbAlarm.setClearKey("clear:oops:1");
        dbAlarm.setLastEventTime(new Date(49L));

        var protoAlarm = AlarmMapper.INSTANCE.toProto(dbAlarm);
        assertThat(protoAlarm.getTenantId(), equalTo(dbAlarm.getTenantId()));
        assertThat(protoAlarm.getDatabaseId(), equalTo(dbAlarm.getAlarmId()));
        assertThat(protoAlarm.getCounter(), equalTo(dbAlarm.getCounter()));
        assertThat(protoAlarm.getReductionKey(), equalTo(dbAlarm.getReductionKey()));
        assertThat(protoAlarm.getClearKey(), equalTo(dbAlarm.getClearKey()));
        assertThat(protoAlarm.getLastUpdateTimeMs(), equalTo(dbAlarm.getLastEventTime().getTime()));
        assertThat(protoAlarm.getIsAcknowledged(), equalTo(false));
    }

    @Test
    public void canMapAcknowledgedAlarm() {
        Alarm dbAlarm = new Alarm();
        dbAlarm.setAcknowledgedByUser("me");
        dbAlarm.setAcknowledgedAt(new Date());

        var protoAlarm = AlarmMapper.INSTANCE.toProto(dbAlarm);
        assertThat(protoAlarm.getIsAcknowledged(), equalTo(true));
        assertThat(protoAlarm.getAckUser(), equalTo(dbAlarm.getAcknowledgedByUser()));
        assertThat(protoAlarm.getAckTimeMs(), equalTo(dbAlarm.getAcknowledgedAt().getTime()));
    }

}

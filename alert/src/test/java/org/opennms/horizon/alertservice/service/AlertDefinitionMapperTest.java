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

package org.opennms.horizon.alertservice.service;

import org.junit.Test;
import org.opennms.horizon.alerts.proto.AlertType;
import org.opennms.horizon.alerts.proto.ManagedObjectType;
import org.opennms.horizon.alertservice.db.entity.AlertDefinition;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class AlertDefinitionMapperTest {

    @Test
    public void canMapAlertDefinition() {
        AlertDefinition dbAlertDefinition = new AlertDefinition();
        dbAlertDefinition.setUei("uei.opennms.org/vendor/cisco/traps/SNMP_Link_U");
        dbAlertDefinition.setReductionKey("%s:%s:%d");
        dbAlertDefinition.setClearKey("%s:uei.opennms.org/vendor/cisco/traps/SNMP_Link_Down:%d");
        dbAlertDefinition.setType(AlertType.CLEAR);
        dbAlertDefinition.setManagedObjectType(ManagedObjectType.UNDEFINED);

        var protoAlertDef = AlertDefinitionMapper.INSTANCE.toProto(dbAlertDefinition);
        assertThat(protoAlertDef.getUei(), equalTo(dbAlertDefinition.getUei()));
        assertThat(protoAlertDef.getReductionKey(), equalTo(dbAlertDefinition.getReductionKey()));
        assertThat(protoAlertDef.getClearKey(), equalTo(dbAlertDefinition.getClearKey()));
        assertThat(protoAlertDef.getType(), equalTo(dbAlertDefinition.getType()));
        assertThat(protoAlertDef.getManagedObjectType(), equalTo(dbAlertDefinition.getManagedObjectType()));
    }

}

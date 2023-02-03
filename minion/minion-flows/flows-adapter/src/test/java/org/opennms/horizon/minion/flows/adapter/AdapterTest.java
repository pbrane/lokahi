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

package org.opennms.horizon.minion.flows.adapter;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.opennms.horizon.minion.flows.adapter.common.AdapterDefinition;
import org.opennms.horizon.minion.flows.adapter.common.PackageDefinition;
import org.opennms.horizon.minion.flows.adapter.common.TelemetryMessageLog;
import org.opennms.horizon.minion.flows.adapter.common.TelemetryMessageLogEntry;
import org.opennms.horizon.minion.flows.adapter.netflow9.Netflow9AdapterFactory;

public class AdapterTest {

    @Test
    public void test1() {
        Netflow9AdapterFactory netflow5AdapterFactory = new Netflow9AdapterFactory();
        AdapterDefinition adapterDefinition = new AdapterDefinition() {
            @Override
            public String getFullName() {
                return null;
            }

            @Override
            public List<? extends PackageDefinition> getPackages() {
                return null;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public String getClassName() {
                return null;
            }

            @Override
            public Map<String, String> getParameterMap() {
                return null;
            }
        };

        TelemetryMessageLog telemetryMessageLog = new TelemetryMessageLog() {
            @Override
            public String getLocation() {
                return null;
            }

            @Override
            public String getSystemId() {
                return null;
            }

            @Override
            public String getSourceAddress() {
                return null;
            }

            @Override
            public int getSourcePort() {
                return 0;
            }

            @Override
            public List<? extends TelemetryMessageLogEntry> getMessageList() {
                return null;
            }
        };
        //netflow5AdapterFactory.setPipeline();
        netflow5AdapterFactory.createBean(adapterDefinition).handleMessageLog(telemetryMessageLog);

    }


}

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.horizon.server.service.metrics;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class MetricLabelUtils {
    public static final String METRIC_NAME_KEY = "__name__";
    public static final String MONITOR_KEY = "monitor";
    private static final String NODE_ID_KEY = "node_id";

    public String getMetricName(Map<String, String> metricLabels) {
        if (metricLabels.containsKey(METRIC_NAME_KEY)) {
            return metricLabels.get(METRIC_NAME_KEY);
        }
        throw new RuntimeException("No Metric Name Found in response labels - shouldn't get here");
    }

    public String getMonitorType(Map<String, String> metricLabels) {
        if (metricLabels.containsKey(MONITOR_KEY)) {
            return metricLabels.get(MONITOR_KEY);
        }
        throw new RuntimeException("No Monitor Type Found");
    }

    public Optional<Long> getNodeId(Map<String, String> metricLabels) {
        if (metricLabels.containsKey(NODE_ID_KEY)) {
            String nodeIdStr = metricLabels.get(NODE_ID_KEY);
            try {
                return Optional.of(Long.parseLong(nodeIdStr));
            } catch (NumberFormatException e) {
                throw new RuntimeException("Node ID is not a number");
            }
        }
        return Optional.empty();
    }
}
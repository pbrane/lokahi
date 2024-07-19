/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.horizon.events.thresholdconsumer.impl;

import static org.opennms.horizon.shared.utils.SystemInfoUtils.CRITICAL;
import static org.opennms.horizon.shared.utils.SystemInfoUtils.FIRING;
import static org.opennms.horizon.shared.utils.SystemInfoUtils.MAJOR;
import static org.opennms.horizon.shared.utils.SystemInfoUtils.MINOR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.opennms.horizon.events.grpc.client.InventoryClient;
import org.opennms.horizon.events.proto.Event;
import org.opennms.horizon.events.proto.EventParameter;
import org.opennms.horizon.events.proto.Severity;
import org.opennms.horizon.events.proto.ThresholdInfo;
import org.opennms.horizon.events.thresholdconsumer.ThresholdAlertToEventMapper;
import org.opennms.horizon.inventory.dto.NodeDTO;
import org.opennms.horizon.metrics.threshold.proto.ThresholdAlert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ThresholdAlertToEventMapperImpl implements ThresholdAlertToEventMapper {

    public static final String HIGH_THRESHOLD_EVENT_UEI = "uei.opennms.org/threshold/highThresholdExceeded";

    ThresholdAlertToEventMapperImpl(@Autowired InventoryClient inventoryClient) {
        this.inventoryClient = inventoryClient;
    }

    private final InventoryClient inventoryClient;

    @Override
    public Event convert(ThresholdAlert alert) {
        Map<String, String> labels = alert.getLabelsMap();
        String tenantId = labels.get("tenant_id");
        NodeDTO nodeDTO = inventoryClient.getNodeById(tenantId, Long.parseLong(labels.get("node_id")));
        Event.Builder eventBuilder = Event.newBuilder();
        eventBuilder
                .setNodeId(Long.parseLong(labels.get("node_id")))
                .setSeverity(
                        alert.getStatus().equalsIgnoreCase(FIRING)
                                ? getSeverity(labels.get("severity").toLowerCase())
                                : Severity.NORMAL)
                .setTenantId(tenantId)
                .setLocationId(String.valueOf(nodeDTO.getMonitoringLocationId()))
                .setLocationName(nodeDTO.getLocation())
                .setUei(HIGH_THRESHOLD_EVENT_UEI)
                .setThresholdInfo(ThresholdInfo.newBuilder()
                        .setAlertName(labels.get("alertname"))
                        .setStatus(alert.getStatus())
                        .build())
                .setProducedTimeMs(System.currentTimeMillis())
                .setEventLabel(Arrays.stream(HIGH_THRESHOLD_EVENT_UEI.split("/"))
                        .reduce((first, second) -> second)
                        .orElse(""));
        if (alert.hasAnnotations()) {
            eventBuilder.setDescription(alert.getAnnotations().getDescription());
        }

        addEventParameters(labels, eventBuilder);

        return eventBuilder.build();
    }

    private static void addEventParameters(Map<String, String> labels, Event.Builder eventBuilder) {
        List<EventParameter> parameters = new ArrayList<>();
        addParameterIfNotEmpty(parameters, "alert_name", labels.get("alertname"));
        addParameterIfNotEmpty(parameters, "status", labels.get("severity"));
        addParameterIfNotEmpty(parameters, "monitor-type", labels.get("monitor"));

        eventBuilder.addAllParameters(parameters);
    }

    private static void addParameterIfNotEmpty(List<EventParameter> parameters, String name, String value) {
        if (value != null && !value.isEmpty()) {
            EventParameter parameter = EventParameter.newBuilder()
                    .setName(name)
                    .setValue(value)
                    .setType("string")
                    .setEncoding("none")
                    .build();
            parameters.add(parameter);
        }
    }

    private static Severity getSeverity(String severity) {

        return switch (severity) {
            case CRITICAL -> Severity.CRITICAL;
            case MAJOR -> Severity.MAJOR;
            case MINOR -> Severity.MINOR;
            default -> Severity.NORMAL;
        };
    }
}

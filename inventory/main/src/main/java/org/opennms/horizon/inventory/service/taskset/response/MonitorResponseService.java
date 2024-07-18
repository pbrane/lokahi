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
package org.opennms.horizon.inventory.service.taskset.response;

import java.time.LocalDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opennms.horizon.events.proto.Event;
import org.opennms.horizon.events.proto.EventLog;
import org.opennms.horizon.events.proto.EventParameter;
import org.opennms.horizon.inventory.component.InternalEventProducer;
import org.opennms.horizon.inventory.model.MonitoredEntityState;
import org.opennms.horizon.inventory.monitoring.MonitoredEntityService;
import org.opennms.horizon.inventory.repository.MonitoredEntityStateRepository;
import org.opennms.horizon.inventory.repository.MonitoredServiceRepository;
import org.opennms.horizon.inventory.repository.MonitoringLocationRepository;
import org.opennms.horizon.shared.events.EventConstants;
import org.opennms.taskset.contract.MonitorResponse;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonitorResponseService {

    private final MonitoredEntityStateRepository monitoredEntityStateRepository;

    private final MonitoredServiceRepository monitoredServiceRepository;

    private final MonitoringLocationRepository monitoringLocationRepository;

    private final MonitoredEntityService monitoredEntityService;

    private final InternalEventProducer eventProducer;

    public void updateMonitoredState(String tenantId, String locationId, MonitorResponse monitorResponse) {
        if (monitorResponse.getMonitorType().equals("ECHO")) {
            // No need to handle Echo monitor response
            return;
        }

        final var monitoredEntity = monitoredEntityService.findServiceById(
                tenantId, Long.parseLong(locationId), monitorResponse.getMonitoredEntityId());
        if (monitoredEntity.isEmpty()) {
            // Did not find the monitored entity for this response
            return;
        }

        var optionalServiceState = monitoredEntityStateRepository.findByTenantIdAndMonitoredEntityId(
                tenantId, monitorResponse.getMonitoredEntityId());
        var previousState = Boolean.TRUE;
        Boolean statusFromMonitor = "Up".equalsIgnoreCase(monitorResponse.getStatus()) ? Boolean.TRUE : Boolean.FALSE;
        if (optionalServiceState.isPresent()) {
            previousState = optionalServiceState.get().getServiceState();
            var monitorServiceState = optionalServiceState.get();
            monitorServiceState.setServiceState(statusFromMonitor);
            monitoredEntityStateRepository.save(monitorServiceState);
        } else {
            MonitoredEntityState monitoredEntityState = new MonitoredEntityState();
            monitoredEntityState.setTenantId(tenantId);
            monitoredEntityState.setMonitoredEntityId(monitorResponse.getMonitoredEntityId());
            monitoredEntityState.setServiceState(statusFromMonitor);
            monitoredEntityState.setFirstObservationTime(LocalDateTime.now());
            monitoredEntityStateRepository.save(monitoredEntityState);
        }
        if (!Objects.equals(statusFromMonitor, previousState)) {
            // State changed, send event
            triggerEvent(tenantId, locationId, monitorResponse, statusFromMonitor);
        }
    }

    private void triggerEvent(
            String tenantId, String locationId, MonitorResponse monitorResponse, boolean statusFromMonitor) {
        var eventBuilder = Event.newBuilder();
        if (statusFromMonitor) {
            eventBuilder.setUei(EventConstants.SERVICE_RESTORED_EVENT_UEI);
        } else {
            eventBuilder.setUei(EventConstants.SERVICE_UNREACHABLE_EVENT_UEI);
        }
        eventBuilder.setTenantId(tenantId);
        if (monitorResponse.getMetricLabelsMap().containsKey("node_id")) {
            eventBuilder.setNodeId(
                    Long.parseLong(monitorResponse.getMetricLabelsMap().get("node_id")));
        }
        eventBuilder.setProducedTimeMs(monitorResponse.getTimestamp());
        eventBuilder.setDescription(monitorResponse.getReason());
        eventBuilder.setLocationId(locationId);
        monitoringLocationRepository
                .findByIdAndTenantId(Long.parseLong(locationId), tenantId)
                .ifPresent(l -> eventBuilder.setLocationName(l.getLocation()));
        eventBuilder.addParameters(EventParameter.newBuilder()
                .setName("monitoredEntityId")
                .setValue(monitorResponse.getMonitoredEntityId()));
        eventBuilder.addParameters(
                EventParameter.newBuilder().setName("serviceName").setValue(monitorResponse.getMonitorType()));
        var eventLog = EventLog.newBuilder().setTenantId(tenantId).addEvents(eventBuilder.build());
        eventProducer.sendEvent(eventLog.build());
    }
}

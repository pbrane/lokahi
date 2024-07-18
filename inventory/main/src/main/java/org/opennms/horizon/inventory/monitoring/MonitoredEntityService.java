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
package org.opennms.horizon.inventory.monitoring;

import com.google.common.base.Joiner;
import com.google.protobuf.Any;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opennms.horizon.inventory.model.DiscoveryMonitoredEntityProvider;
import org.opennms.horizon.inventory.repository.MonitoringLocationRepository;
import org.opennms.horizon.inventory.service.taskset.TaskUtils;
import org.opennms.horizon.inventory.service.taskset.publisher.TaskSetPublisher;
import org.opennms.taskset.contract.MonitorSetConfig;
import org.opennms.taskset.contract.TaskDefinition;
import org.opennms.taskset.contract.TaskType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoredEntityService {

    private final List<MonitoredEntityProvider> providers;
    private final MonitoringLocationRepository monitoringLocationRepository;
    private final TaskSetPublisher taskSetPublisher;

    public List<MonitoredEntity> getAllMonitoredEntities(final String tenantId, final long locationId) {
        return this.providers.stream()
                .flatMap(provider -> provider.getMonitoredEntities(tenantId, locationId).stream())
                .toList();
    }

    public List<MonitoredEntity> getAllMonitoredEntities(final String tenantId) {
        return this.providers.stream()
                .flatMap(provider -> monitoringLocationRepository.findByTenantId(tenantId).stream()
                        .flatMap(location -> provider.getMonitoredEntities(tenantId, location.getId()).stream()))
                .toList();
    }

    public Optional<MonitoredEntity> findServiceById(final String tenantId, final long locationId, final String id) {
        return this.providers.stream()
                .flatMap(provider -> provider.getMonitoredEntities(tenantId, locationId).stream())
                .filter(entity -> entity.getId().equals(id))
                .findAny();
    }

    public void publishTaskSet(final String tenantId, final long locationId) {
        final var taskGroups = this.getAllMonitoredEntities(tenantId, locationId).stream()
                .filter(me -> !me.getSource().getProviderId().equals(DiscoveryMonitoredEntityProvider.ID))
                .collect(Collectors.groupingBy(me -> me.getSource().getProviderId()));

        final var tasks = taskGroups.entrySet().stream()
                .map(taskGroup -> {
                    final var configuration = MonitorSetConfig.newBuilder();

                    taskGroup.getValue().stream()
                            .map(monitoredEntity -> MonitorSetConfig.MonitorConfig.newBuilder()
                                    .setMonitoredEntityId(monitoredEntity.getId())
                                    .setMonitorType(monitoredEntity.getType() + "Monitor")
                                    .setConfiguration(monitoredEntity.getConfig())
                                    .build())
                            .forEach(configuration::addMonitorConfig);

                    return TaskDefinition.newBuilder()
                            .setId(Joiner.on(":").join("monitor", tenantId, locationId, taskGroup.getKey()))
                            .setType(TaskType.MONITOR)
                            .setSchedule(TaskUtils.DEFAULT_SCHEDULE)
                            .setConfiguration(Any.pack(configuration.build()))
                            .build();
                })
                .toList();

        this.taskSetPublisher.publishNewTasks(tenantId, locationId, tasks);
        log.info("PublishTaskSet executed with tenantId={}, locationId={}", tenantId, locationId);
    }

    public void publishTaskSetForDiscovery(
            final String tenantId, final long locationId, MonitoredEntity monitoredEntity, Map<String, String> labels) {

        MonitorSetConfig.MonitorConfig monitorConfig = MonitorSetConfig.MonitorConfig.newBuilder()
                .setMonitoredEntityId(monitoredEntity.getId())
                .setMonitorType(monitoredEntity.getType() + "Monitor")
                .setConfiguration(monitoredEntity.getConfig())
                .build();
        MonitorSetConfig monitorSetConfig =
                MonitorSetConfig.newBuilder().addMonitorConfig(monitorConfig).build();

        TaskDefinition task = TaskDefinition.newBuilder()
                .setId(Joiner.on(":").join("monitor", tenantId, locationId, monitoredEntity.getId()))
                .setType(TaskType.MONITOR)
                .setSchedule(TaskUtils.DEFAULT_SCHEDULE)
                .setConfiguration(Any.pack(monitorSetConfig))
                .putAllMetricLabels(labels)
                .build();

        this.taskSetPublisher.publishNewTasks(tenantId, locationId, List.of(task));
    }
}

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
package org.opennms.horizon.inventory.monitoring.simple.config;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opennms.horizon.inventory.dto.SimpleMonitoredEntityResponse;
import org.opennms.horizon.inventory.monitoring.simple.SimpleMonitoredActiveDiscovery;
import org.opennms.horizon.inventory.monitoring.simple.SimpleMonitoredEntityMapper;
import org.opennms.horizon.inventory.repository.SimpleMonitoredEntityRepository;
import org.opennms.horizon.inventory.repository.discovery.active.ActiveDiscoveryRepository;
import org.opennms.horizon.inventory.service.MonitoringLocationService;
import org.opennms.horizon.inventory.service.TagService;
import org.opennms.horizon.inventory.service.discovery.active.ActiveDiscoveryValidationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimpleMonitorDiscoveryService implements ActiveDiscoveryValidationService {

    private final SimpleMonitoredEntityRepository repository;
    private final ActiveDiscoveryRepository activeDiscoveryRepository;
    private final MonitoringLocationService monitoringLocationService;
    private final SimpleMonitoredEntityMapper mapper;
    private final TagService tagService;

    @Transactional
    public SimpleMonitoredEntityResponse createActiveDiscovery(SimpleMonitoredActiveDiscovery entity, String tenantId) {

        validateActiveDiscoveryName(entity.getName(), tenantId);
        validateLocation(entity.getLocationId().toString(), tenantId);

        return this.mapper.map(this.repository.save(entity));

        // Todo : implement logic for tags LOK: 2720
        /*tagService.addTags(
        tenantId,
        TagCreateListDTO.newBuilder()
            .addEntityIds(TagEntityIdDTO.newBuilder().setActiveDiscoveryId(Long.parseLong(response.getId())))
            .addAllTags(List.of(TagCreateDTO.newBuilder().setName("default").build()))
            .build());*/

    }

    public Optional<SimpleMonitoredEntityResponse> getDiscoveryById(long id, String tenantId) {
        var optional = repository.findByIdAndTenantId(id, tenantId);
        return optional.map(mapper::map);
    }

    @Override
    public ActiveDiscoveryRepository getActiveDiscoveryRepository() {
        return activeDiscoveryRepository;
    }

    @Override
    public MonitoringLocationService getMonitoringLocationService() {
        return monitoringLocationService;
    }
}

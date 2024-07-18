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
package org.opennms.horizon.inventory.model;

import com.google.common.base.Strings;
import com.google.protobuf.Any;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opennms.azure.contract.AzureMonitorRequest;
import org.opennms.horizon.inventory.monitoring.MonitoredEntity;
import org.opennms.horizon.inventory.monitoring.MonitoredEntityProvider;
import org.opennms.horizon.inventory.repository.NodeRepository;
import org.opennms.horizon.inventory.service.taskset.TaskUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AzureMonitoredEntityProvider implements MonitoredEntityProvider {
    private static final String ID = "azure";

    private final NodeRepository nodeRepository;

    @Override
    public String getProviderId() {
        return ID;
    }

    @Override
    @Transactional
    public List<MonitoredEntity> getMonitoredEntities(final String tenantId, final long locationId) {
        return nodeRepository.findByTenantIdAndLocationId(tenantId, locationId).stream()
                .filter(node -> !Strings.isNullOrEmpty(node.getAzureResource()))
                .map(node -> {
                    Any config = Any.pack(AzureMonitorRequest.newBuilder()
                            .setResource(node.getAzureResource())
                            .setResourceGroup(node.getAzureResourceGroup())
                            .setClientId(node.getAzureClientId())
                            .setClientSecret(node.getAzureClientSecret())
                            .setSubscriptionId(node.getAzureSubscriptionId())
                            .setDirectoryId(node.getAzureDirectoryId())
                            .setTimeoutMs(TaskUtils.AZURE_DEFAULT_TIMEOUT_MS)
                            .setRetries(TaskUtils.AZURE_DEFAULT_RETRIES)
                            .setNodeId(node.getId())
                            .build());

                    return MonitoredEntity.builder()
                            .source(this)
                            .entityId(Long.toString(node.getId()))
                            .locationId(locationId)
                            .type("AZURE")
                            .config(config)
                            .build();
                })
                .toList();
    }
}

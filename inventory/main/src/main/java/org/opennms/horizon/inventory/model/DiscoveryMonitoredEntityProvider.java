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

import com.google.protobuf.Any;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opennms.horizon.inventory.monitoring.MonitoredEntity;
import org.opennms.horizon.inventory.monitoring.MonitoredEntityProvider;
import org.opennms.horizon.inventory.repository.MonitoredServiceRepository;
import org.opennms.horizon.inventory.service.SnmpConfigService;
import org.opennms.horizon.inventory.service.taskset.TaskUtils;
import org.opennms.horizon.shared.utils.InetAddressUtils;
import org.opennms.icmp.contract.IcmpMonitorRequest;
import org.opennms.snmp.contract.SnmpMonitorRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscoveryMonitoredEntityProvider implements MonitoredEntityProvider {
    public static final String ID = "discovery";

    private final MonitoredServiceRepository serviceRepository;
    private final SnmpConfigService snmpConfigService;

    @Override
    public String getProviderId() {
        return ID;
    }

    @Override
    @Transactional
    public List<MonitoredEntity> getMonitoredEntities(final String tenantId, final long locationId) {
        return this.serviceRepository.findByTenantIdAndLocationId(tenantId, locationId).stream()
                .filter(service -> service.getIpInterface().getSnmpPrimary())
                .map(service -> MonitoredEntity.builder()
                        .source(this)
                        .entityId(createMonitoredEntityId(service))
                        .locationId(locationId)
                        .type(service.getMonitorType())
                        .config(
                                switch (service.getMonitorType()) {
                                    case "ICMP" -> Any.pack(IcmpMonitorRequest.newBuilder()
                                            .setHost(InetAddressUtils.toIpAddrString(
                                                    service.getIpInterface().getIpAddress()))
                                            .setTimeout(TaskUtils.ICMP_DEFAULT_TIMEOUT_MS)
                                            .setDscp(TaskUtils.ICMP_DEFAULT_DSCP)
                                            .setAllowFragmentation(TaskUtils.ICMP_DEFAULT_ALLOW_FRAGMENTATION)
                                            .setPacketSize(TaskUtils.ICMP_DEFAULT_PACKET_SIZE)
                                            .setRetries(TaskUtils.ICMP_DEFAULT_RETRIES)
                                            .build());
                                    case "SNMP" -> {
                                        final var snmpConfig = this.snmpConfigService.getSnmpConfig(
                                                tenantId,
                                                locationId,
                                                service.getIpInterface().getIpAddress());
                                        yield Any.pack(SnmpMonitorRequest.newBuilder()
                                                .setHost(InetAddressUtils.toIpAddrString(
                                                        service.getIpInterface().getIpAddress()))
                                                .setAgentConfig(snmpConfig.orElse(null))
                                                .build());
                                    }
                                    default -> {
                                        log.warn("Unknown monitor type: {}", service.getMonitorType());
                                        yield null;
                                    }
                                })
                        .build())
                .toList();
    }

    public static String createMonitoredEntityId(MonitoredService monitoredService) {
        return Long.toString(monitoredService.getId());
    }
}

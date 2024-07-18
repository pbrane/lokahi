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
package org.opennms.horizon.inventory.monitoring.simple;

import com.google.gson.Gson;
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opennms.horizon.inventory.monitoring.MonitoredEntity;
import org.opennms.horizon.inventory.monitoring.MonitoredEntityProvider;
import org.opennms.horizon.inventory.monitoring.simple.config.HttpMonitorConfig;
import org.opennms.horizon.inventory.monitoring.simple.config.HttpsMonitorConfig;
import org.opennms.horizon.inventory.monitoring.simple.config.IcmpMonitorConfig;
import org.opennms.horizon.inventory.monitoring.simple.config.MonitorConfig;
import org.opennms.horizon.inventory.monitoring.simple.config.MonitorConfigMapper;
import org.opennms.horizon.inventory.monitoring.simple.config.NtpMonitorConfig;
import org.opennms.horizon.inventory.monitoring.simple.config.SshMonitorConfig;
import org.opennms.horizon.inventory.repository.SimpleMonitoredEntityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimpleMonitoredEntityProvider implements MonitoredEntityProvider {
    public static String PROVIDER_ID = "simple";

    private final SimpleMonitoredEntityRepository repository;

    private final MonitorConfigMapper mapper;

    private static final Gson GSON = new Gson();

    @Override
    public String getProviderId() {
        return PROVIDER_ID;
    }

    @Override
    @Transactional
    public List<MonitoredEntity> getMonitoredEntities(final String tenantId, final long locationId) {
        return this.repository.findByTenantIdAndLocationId(tenantId, locationId).stream()
                .map(entity -> MonitoredEntity.builder()
                        .source(this)
                        .locationId(locationId)
                        .entityId(entity.getId().toString())
                        .type(entity.getType())
                        .config(
                                switch (entity.getType()) {
                                    case "HTTP" -> jsonConfig(
                                            entity.getConfig(), HttpMonitorConfig.class, this.mapper::map);
                                    case "HTTPS" -> jsonConfig(
                                            entity.getConfig(), HttpsMonitorConfig.class, this.mapper::map);
                                    case "ICMP" -> jsonConfig(
                                            entity.getConfig(), IcmpMonitorConfig.class, this.mapper::map);
                                    case "NTP" -> jsonConfig(
                                            entity.getConfig(), NtpMonitorConfig.class, this.mapper::map);
                                    case "SSH" -> jsonConfig(
                                            entity.getConfig(), SshMonitorConfig.class, this.mapper::map);
                                    default -> {
                                        log.error("Unknown monitored entity type: {}", entity.getType());
                                        yield null;
                                    }
                                })
                        .build())
                .toList();
    }

    private static <C extends MonitorConfig, R extends Message> Any jsonConfig(
            final String json, final Class<C> configClass, final Function<C, R> mapper) {
        final C config = GSON.fromJson(json, configClass);
        final R request = mapper.apply(config);

        return Any.pack(request);
    }
}

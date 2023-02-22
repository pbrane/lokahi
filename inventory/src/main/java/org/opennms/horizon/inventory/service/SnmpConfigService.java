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
package org.opennms.horizon.inventory.service;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.Map;

import org.opennms.horizon.inventory.dto.ConfigKey;
import org.opennms.horizon.inventory.dto.ConfigurationDTO;
import org.opennms.horizon.inventory.model.Configuration;
import org.opennms.horizon.inventory.service.snmpconfig.SnmpConfigBean;
import org.opennms.horizon.shared.snmp.SnmpAgentConfig;
import org.opennms.horizon.shared.snmp.conf.xml.SnmpConfig;
import org.opennms.horizon.shared.snmp.config.SnmpPeerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SnmpConfigService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConfigurationService configurationService;
    private final SnmpPeerFactory snmpPeerFactory;

    public SnmpConfigService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
        this.snmpPeerFactory = SnmpPeerFactory.getInstance();
    }

    public Configuration persistSnmpConfig(File resource, String location, String tenantId) {
        try {
            // Use try-with-resources to read the file and close the resources automatically
            String json = Files.readString(resource.toPath());

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> map = mapper.readValue(json, new TypeReference<>() {
            });

            // Set configuration parameters for SnmpConfig
            SnmpConfig snmpConfig = new SnmpConfig();
            snmpConfig.setTimeout((Integer) map.get(SnmpPeerFactory.TIMEOUT_KEY));
            snmpConfig.setVersion((String) map.get(SnmpPeerFactory.VERSION_KEY));
            snmpConfig.setRetry((Integer) map.get(SnmpPeerFactory.RETRY_KEY));
            snmpConfig.setReadCommunity((String) map.get(SnmpPeerFactory.READ_COMMUNITY_KEY));
            snmpPeerFactory.setM_config(snmpConfig);
            ConfigurationDTO configDto = mapSnmpConfigToConfigurationDTO(snmpConfig, tenantId, location);
            return configurationService.createSingle(configDto);
        } catch (IOException e) {
            log.error("An error occurred while reading the file: {}", resource, e);
        } catch (Exception e) {
            log.error("An error occurred while creating SnmpPeerFactory instance: {}", e.getMessage(), e);
        }
        return null;
    }

    public SnmpAgentConfig getSnmpAgentConfig(InetAddress address, String location) {
        return snmpPeerFactory.getAgentConfig(address, location);
    }

    public SnmpAgentConfig getSnmpAgentConfig(InetAddress address) {
        return snmpPeerFactory.getAgentConfig(address);
    }

    private ConfigurationDTO mapSnmpConfigToConfigurationDTO(SnmpConfig config, String tenantId, String location) {
        return ConfigurationDTO.newBuilder()
            .setKey(ConfigKey.SNMP)
            .setTenantId(tenantId)
            .setValue(mapSnmpConfigToJson(config))
            .setLocation(location)
            .build();
    }

    private String mapSnmpConfigToJson(SnmpConfig config) {
        SnmpConfigBean snmpConfigBean = new SnmpConfigBean();
        snmpConfigBean.setTimeout(config.getTimeout());
        snmpConfigBean.setRetry(config.getRetry());
        snmpConfigBean.setReadCommunity(config.getReadCommunity());
        snmpConfigBean.setVersion(config.getVersion());
        try {
            return objectMapper.writeValueAsString(snmpConfigBean);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

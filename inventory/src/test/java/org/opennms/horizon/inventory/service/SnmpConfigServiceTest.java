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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.horizon.inventory.dto.ConfigurationDTO;
import org.opennms.horizon.inventory.model.Configuration;
import org.opennms.horizon.inventory.service.snmpconfig.SnmpConfigBean;
import org.opennms.horizon.shared.snmp.conf.xml.SnmpConfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SnmpConfigServiceTest {

    private SnmpConfigService service;
    private ConfigurationService configurationService;
    private SnmpConfig snmpConfig;
    private SnmpConfigBean snmpConfigBean;
    private Configuration configuration;

    @Before
    public void setUp() throws JsonProcessingException {
        configurationService = mock(ConfigurationService.class);
        service = new SnmpConfigService(configurationService);

        snmpConfig = new SnmpConfig();
        snmpConfig.setVersion("v2c");
        snmpConfig.setRetry(1);
        snmpConfig.setReadCommunity("public");
        snmpConfig.setTimeout(3000);

        snmpConfigBean = new SnmpConfigBean();
        snmpConfigBean.setVersion("v2c");
        snmpConfigBean.setReadCommunity("public");
        snmpConfigBean.setTimeout(3000);
        snmpConfigBean.setRetry(1);

        configuration = new Configuration();
        configuration.setKey(ConfigurationService.SNMP_CONFIG);
        configuration.setTenantId("tenantId");
        configuration.setLocation("location");
        configuration.setValue(new ObjectMapper().readTree(new ObjectMapper().writeValueAsString(snmpConfigBean)));

        when(configurationService.createSingle(any(ConfigurationDTO.class))).thenReturn(configuration);
    }

    @Test
    public void persistSnmpConfigTest() throws JsonProcessingException {
        ArgumentCaptor<ConfigurationDTO> captor = ArgumentCaptor.forClass(ConfigurationDTO.class);
        when(configurationService.createSingle(captor.capture())).thenReturn(configuration);
        Configuration ret = service.persistSnmpConfig(snmpConfig, "tenantId", "location");
        assertThat(captor.getValue().getKey()).isEqualTo(ConfigurationService.SNMP_CONFIG);
        assertThat(captor.getValue().getTenantId()).isEqualTo("tenantId");
        assertThat(captor.getValue().getLocation()).isEqualTo("location");
        assertThat(captor.getValue().getValue()).isEqualTo(new ObjectMapper().writeValueAsString(snmpConfigBean));

        assertThat(ret).isNotNull();
        assertThat(ret).isEqualTo(configuration);
    }
}

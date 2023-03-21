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

package org.opennms.horizon.notifications.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opennms.horizon.alerts.proto.Alert;
import org.opennms.horizon.model.common.proto.Severity;
import org.opennms.horizon.notifications.dto.PagerDutyConfigDTO;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.any;

@RunWith(MockitoJUnitRunner.class)
public class PagerDutyAPITest {
    @InjectMocks
    PagerDutyAPI pagerDutyAPI;

    @Mock
    RestTemplate restTemplate;

    @Mock
    PagerDutyDao pagerDutyDao;

    @Test
    public void postNotifications() throws Exception {
        Mockito.when(pagerDutyDao.getConfig(any())).thenReturn(getConfigDTO());
        Alert alert = getAlert();
        pagerDutyAPI.postNotification(alert);
    }

    @Test
    public void saveConfig() {
        pagerDutyAPI.saveConfig(getConfigDTO());
    }

    private PagerDutyConfigDTO getConfigDTO() {
        return PagerDutyConfigDTO.newBuilder().setIntegrationKey("integration_key").build();
    }

    private Alert getAlert() {
        return Alert.newBuilder()
            .setLogMessage("Exciting message to go here")
            .setReductionKey("srv01/mysql")
            .setSeverity(Severity.MAJOR)
            .build();
    }
}

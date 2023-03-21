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

package org.opennms.horizon.notifications.kafka;

import org.opennms.horizon.notifications.service.NotificationService;
import org.opennms.horizon.notifications.tenant.TenantContext;
import org.opennms.horizon.notifications.tenant.WithTenant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.opennms.horizon.notifications.dto.PagerDutyConfigDTO;

@Component
public class AlertKafkaConsumerTestHelper {

    @Autowired
    private NotificationService notificationService;

    @WithTenant(tenantIdArg = 0)
    public void setupConfig(String tenantId) {
        // WithTenant annotation does not function for calls that are internal to the class, hence the need for a helper.
        // It's due to the fact that Aspects use proxies that are only available for calls between different classes.
        // If you really wish to use an internal method, don't use the aspect, and use the line below:
        // try (TenantContext tc = TenantContext.withTenantId(tenantId)) {
        String integrationKey = "not_verified";

        PagerDutyConfigDTO config = PagerDutyConfigDTO.newBuilder().setIntegrationKey(integrationKey).setTenantId(tenantId).build();
        notificationService.postPagerDutyConfig(config);
    }
}

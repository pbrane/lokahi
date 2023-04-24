/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022-2023 The OpenNMS Group, Inc.
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

package org.opennms.horizon.minion.plugin.api.registries;

import com.savoirtech.eos.pattern.whiteboard.KeyedWhiteboard;
import com.savoirtech.eos.util.ServiceProperties;
import lombok.extern.slf4j.Slf4j;
import org.opennms.horizon.minion.plugin.api.PluginMetadata;
import org.opennms.horizon.minion.plugin.api.RegistrationService;
import org.opennms.taskset.contract.TaskType;
import org.osgi.framework.BundleContext;

@Slf4j
public class AlertingPluginRegistry<K, S> extends KeyedWhiteboard<K, S>  {
    private final RegistrationService registrationService;

    public AlertingPluginRegistry(BundleContext bundleContext, Class<S> serviceType, String id, RegistrationService alertingService) {
        super(bundleContext, serviceType, (svc, props) -> props.getProperty(id));
        this.registrationService = alertingService;
        super.start();
    }

    @Override
    protected K addService(S service, ServiceProperties props) {
        K serviceId = super.addService(service, props);

        if (serviceId != null) {
            log.info("Performing scan on service {}", service.getClass());
            PluginMetadata pluginMetadata = new PluginMetadata(serviceId.toString(), TaskType.DETECTOR);
            if (registrationService != null) {
                registrationService.notifyOfPluginRegistration(pluginMetadata);
            }
        }

        return serviceId;
    }

    @Override
    public void start() {

    }
}

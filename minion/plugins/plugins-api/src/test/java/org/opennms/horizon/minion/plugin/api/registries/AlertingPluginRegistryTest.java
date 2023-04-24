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

import com.savoirtech.eos.util.ServiceProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opennms.horizon.minion.plugin.api.PluginMetadata;
import org.opennms.horizon.minion.plugin.api.ServiceDetector;
import org.opennms.horizon.minion.plugin.api.ServiceDetectorManager;
import org.opennms.horizon.minion.plugin.api.RegistrationService;
import org.opennms.taskset.contract.TaskType;
import org.osgi.framework.BundleContext;

public class AlertingPluginRegistryTest {

    private static final String TEST_ID = "test.id";
    
    @Mock
    RegistrationService registrationService;
    @Mock
    BundleContext bundleContext;
    @Mock
    ServiceProperties serviceProperties;

    TestRegistry testRegistry;

    @Captor
    ArgumentCaptor<PluginMetadata> pluginMetadataCaptor;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);

        Mockito.when(serviceProperties.getServiceId()).thenReturn(10L);
        Mockito.when(serviceProperties.getProperty(ArgumentMatchers.anyString())).thenReturn(TEST_ID);
        Mockito.doNothing().when(registrationService).notifyOfPluginRegistration(pluginMetadataCaptor.capture());

        testRegistry = new TestRegistry(bundleContext,"blah", registrationService);
    }

    @Test
    public void addService() {

        testRegistry.addService(new TestPlugin(), serviceProperties);

        Mockito.verify(registrationService).notifyOfPluginRegistration(ArgumentMatchers.any(PluginMetadata.class));
        PluginMetadata pluginMetadata = pluginMetadataCaptor.getValue();
        Assert.assertNotNull(pluginMetadata);
        Assert.assertEquals(TEST_ID, pluginMetadata.getPluginName());
        Assert.assertEquals(TaskType.DETECTOR, pluginMetadata.getPluginType());
        //assertEquals(1, pluginMetadata.getFieldConfigs().size());
    }

    private class TestPlugin implements ServiceDetectorManager {

        @Override
        public ServiceDetector create() {
            return null;
        }
    }

    private class  TestRegistry extends AlertingPluginRegistry<String, ServiceDetectorManager> {

        public TestRegistry(BundleContext bundleContext, String id, RegistrationService alertingService) {
            super(bundleContext, ServiceDetectorManager.class, id, alertingService);
        }
    }
}

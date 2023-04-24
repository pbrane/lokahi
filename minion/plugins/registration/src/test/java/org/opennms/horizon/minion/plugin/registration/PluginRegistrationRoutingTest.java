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

package org.opennms.horizon.minion.plugin.registration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opennms.horizon.minion.plugin.api.PluginMetadata;
import org.opennms.horizon.minion.registration.proto.PluginConfigMessage;
import org.opennms.horizon.shared.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.horizon.shared.ipc.sink.api.SyncDispatcher;
import org.opennms.taskset.contract.TaskType;

public class PluginRegistrationRoutingTest extends CamelTestSupport {

    @Mock
    MessageDispatcherFactory messageDispatcherFactory;

    @Mock
    SyncDispatcher<PluginConfigMessage> dispatcher;

    private String uri = "direct:blah";
    private ProducerTemplate template;
    private long aggregationDelay = 1000;
    private long additionalTestDelay = 1500;

    @Captor
    private ArgumentCaptor<PluginConfigMessage> pluginConfigMessageArgumentCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        super.setUp();

        template = context().createProducerTemplate();
    }

    @Override
    public RouteBuilder createRouteBuilder() throws Exception
    {
        when(messageDispatcherFactory.createSyncDispatcher(any(PluginConfigSinkModule.class))).thenReturn(dispatcher);

        return new PluginRegistrationRouting(uri, messageDispatcherFactory, aggregationDelay);
    }

    @Test
    public void goodDetection() throws Exception {
        doNothing().when(dispatcher).send(pluginConfigMessageArgumentCaptor.capture());
        template.sendBody(uri, new PluginMetadata("blah", TaskType.DETECTOR));

        NotifyBuilder notify = new NotifyBuilder(context()).fromRoute(PluginRegistrationRouting.ROUTE_ID).waitTime(aggregationDelay+additionalTestDelay).whenDone(1).create();
        boolean done = notify.matchesWaitTime();

        assertTrue("Exchange didn't complete", done);

        verify(dispatcher).send(any());
        PluginConfigMessage pluginConfigMessage = pluginConfigMessageArgumentCaptor.getValue();
        assertNotNull(pluginConfigMessage);
        assertEquals(1, pluginConfigMessage.getPluginconfigsCount());
    }

    @Test
    public void badDetection() throws Exception {

        template.sendBody(uri, null);

        NotifyBuilder notify = new NotifyBuilder(context()).fromRoute(PluginRegistrationRouting.ROUTE_ID).waitTime(aggregationDelay+additionalTestDelay).whenDone(1).create();
        boolean done = notify.matchesWaitTime();

        assertTrue("Exchange didn't complete", done);
        verifyNoInteractions(dispatcher);
    }
}

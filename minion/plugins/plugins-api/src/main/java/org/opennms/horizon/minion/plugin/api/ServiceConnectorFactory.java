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

package org.opennms.horizon.minion.plugin.api;

import com.google.protobuf.Any;

import java.util.function.Consumer;

/**
 * Interface for a ListenerFactory, which constructs Listeners.
 */
public interface ServiceConnectorFactory {
    /**
     * Create a new connector that will be used to connect to the remote.
     *
     * @param resultProcessor consumer of ServiceMonitorResponse results to be called as samples are received over the
     *                        connection
     * @param config configuration for the connector
     * @param disconnectHandler runnable executed on disconnect that enables the Minion to schedule reconnection
     *                          attempts; should only be called on disconnect after a successful connect attempt, and
     *                          not when the connect() method on the ServiceConnector throws an exception.
     * @return
     */
    ServiceConnector create(Consumer<ServiceMonitorResponse> resultProcessor, Any config, Runnable disconnectHandler);
}

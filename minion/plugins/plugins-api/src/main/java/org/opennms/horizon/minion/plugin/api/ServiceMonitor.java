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

import java.util.concurrent.CompletableFuture;

public interface ServiceMonitor {

    /**
     * <P>
     * This method is the heart of the plug-in monitor. Each time an interface
     * requires a check to be performed as defined by the scheduler the poll
     * method is invoked. The poll is passed the service to check.
     * </P>
     *
     * <P>
     * By default when the status transition from up to down or vice versa the
     * framework will generate an event. Additionally, if the polling interval
     * changes due to an extended unavailability, the framework will generate an
     * additional down event. The plug-in can suppress the generation of the
     * default events by setting the suppress event bit in the returned integer.
     * </P>
     *

     * @param svc
     *            Includes details about to the service being monitored.
     * @param config
     *            Includes the service parameters
     * @return The availability of the interface and if a transition event
     *         should be suppressed.
     * @exception RuntimeException
     *                Thrown if an unrecoverable error occurs that prevents the
     *                interface from being monitored.
     * @see PollStatus#SERVICE_AVAILABLE
     * @see PollStatus#SERVICE_UNAVAILABLE
     * @see PollStatus#SERVICE_AVAILABLE
     * @see PollStatus#SERVICE_UNAVAILABLE
     */
    public CompletableFuture<ServiceMonitorResponse> poll(MonitoredService svc, Any config);



    /**
     * Allows the monitor to override the location at which it should be run.
     *
     * @param location
     *            location associated with the service to be monitored
     * @return a possibly updated location
     */
    public String getEffectiveLocation(String location);

}

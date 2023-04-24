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

import org.opennms.taskset.contract.MonitorType;

import java.util.Map;

public interface ServiceMonitorResponse {
    /**
     *
     * @return type of monitor that produced the response.
     */
    MonitorType getMonitorType();

    /**
     *
     * @return status whether service is Unknown/Up/Down/Unresponsive
     */
    Status getStatus();

    /**
     *
     * @return reason behind the current poll status when the service is not Up
     */
    String getReason();

    /**
     *
     * @return IP address that was monitored
     */
    String getIpAddress();


    long getNodeId();

    /**
     *  TODO: standardize the unit (ms or sec?)
     *
     * @return amount of time device took to respond to the monitor request
     */
    double getResponseTime();

    Map<String, Number> getProperties();

    /**
     * TECHDEBT: Was originally added to the monitor interface to take advantage of poller's scheduling and
     * configuration mechanism.
     */
    DeviceConfig getDeviceConfig();

    /**
     * Returns timestamp when response time was actually generated.
     *
     * @return Timestamp of a response.
     */
    long getTimestamp();

    interface DeviceConfig {

        byte[] getContent();

        String getFilename();

    }

    enum Status {
        /**
         * Was unable to determine the status.
         */
        Unknown,

        /**
         * Was in a normal state.
         */
        Up,

        /**
         * Not working normally.
         */
        Down,

        /**
         * Service that is up but is most likely suffering due to excessive load or latency
         * issues and because of that has not responded within the configured timeout period.
         */
        Unresponsive;
    }
}

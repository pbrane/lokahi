/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.horizon.minion.heartbeat;

import org.opennms.horizon.grpc.heartbeat.contract.HeartbeatMessage;
import org.opennms.horizon.shared.ipc.sink.api.AggregationPolicy;
import org.opennms.horizon.shared.ipc.sink.api.AsyncPolicy;
import org.opennms.horizon.shared.ipc.sink.api.SinkModule;

public class HeartbeatModule implements SinkModule<HeartbeatMessage, HeartbeatMessage> {

    public static final String MODULE_ID = "Heartbeat";

    @Override
    public String getId() {
        return MODULE_ID;
    }

    @Override
    public int getNumConsumerThreads() {
        return 1;
    }

    @Override
    public byte[] marshal(HeartbeatMessage message) {
        return new byte[0];
    }

    @Override
    public HeartbeatMessage unmarshal(byte[] message) {
        return null;
    }

    @Override
    public byte[] marshalSingleMessage(HeartbeatMessage message) {
        return new byte[0];
    }

    @Override
    public HeartbeatMessage unmarshalSingleMessage(byte[] message) {
        return null;
    }

    @Override
    public AggregationPolicy<HeartbeatMessage, HeartbeatMessage, ?> getAggregationPolicy() {
        // TODO MMF: Is null correct for no aggregation?
        return null;
    }

    @Override
    public AsyncPolicy getAsyncPolicy() {
        return null;
    }
}

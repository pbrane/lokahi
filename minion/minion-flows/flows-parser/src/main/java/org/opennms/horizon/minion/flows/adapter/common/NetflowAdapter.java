/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.horizon.minion.flows.adapter.common;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.opennms.horizon.grpc.telemetry.contract.TelemetryMessage;
import org.opennms.horizon.minion.flows.adapter.imported.Flow;
import org.opennms.horizon.minion.flows.parser.TelemetryRegistry;
import org.opennms.horizon.minion.flows.parser.flowmessage.FlowMessage;
import org.opennms.horizon.minion.flows.parser.flowmessage.NetflowVersion;
import org.opennms.sink.flows.contract.AdapterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.protobuf.InvalidProtocolBufferException;

public class NetflowAdapter extends AbstractFlowAdapter<FlowMessage> {

    private static final Logger LOG = LoggerFactory.getLogger(NetflowAdapter.class);

    public NetflowAdapter(final AdapterConfig adapterConfig,
                          final TelemetryRegistry telemetryRegistry) {
        super(adapterConfig, telemetryRegistry);
    }

    @Override
    protected FlowMessage parse(TelemetryMessage message) {
        try {
            return FlowMessage.parseFrom(message.getBytes());
        } catch (InvalidProtocolBufferException e) {
            LOG.error("Unable to parse message from proto", e);
        }
        return null;
    }

    @Override
    public List<Flow> convert(final FlowMessage packet, final Instant receivedAt) {
        return Collections.singletonList(new NetflowMessage(packet, receivedAt));
    }
}

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

import static com.codahale.metrics.MetricRegistry.name;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.opennms.horizon.grpc.telemetry.contract.TelemetryMessage;
import org.opennms.horizon.minion.flows.adapter.imported.ContextKey;
import org.opennms.horizon.minion.flows.adapter.imported.Flow;
import org.opennms.horizon.minion.flows.adapter.imported.FlowSource;
import org.opennms.horizon.minion.flows.parser.TelemetryRegistry;
import org.opennms.sink.flows.contract.AdapterConfig;
import org.opennms.sink.flows.contract.PackageConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.google.common.base.Strings;

public abstract class AbstractFlowAdapter<P> implements Adapter {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractFlowAdapter.class);

    private String metaDataNodeLookup;
    private ContextKey contextKey;

    /**
     * Time taken to parse a log
     */
    private final Timer logParsingTimer;

    /**
     * Number of packets per log
     */
    private final Histogram packetsPerLogHistogram;

    private final Meter entriesReceived;

    private final Meter entriesParsed;

    private final Meter entriesConverted;

    private boolean applicationThresholding;
    private boolean applicationDataCollection;

    private final List<PackageConfig> packages;

    private TelemetryRegistry telemetryRegistry;


    public AbstractFlowAdapter(final AdapterConfig adapterConfig,
                               final TelemetryRegistry telemetryRegistry) {
        Objects.requireNonNull(adapterConfig);
        Objects.requireNonNull(telemetryRegistry.getMetricRegistry());

        this.logParsingTimer = telemetryRegistry.getMetricRegistry().timer(name("adapters", adapterConfig.getName(), "logParsing"));
        this.packetsPerLogHistogram = telemetryRegistry.getMetricRegistry().histogram(name("adapters", adapterConfig.getName(), "packetsPerLog"));
        this.entriesReceived = telemetryRegistry.getMetricRegistry().meter(name("adapters", adapterConfig.getName(), "entriesReceived"));
        this.entriesParsed = telemetryRegistry.getMetricRegistry().meter(name("adapters", adapterConfig.getName(), "entriesParsed"));
        this.entriesConverted = telemetryRegistry.getMetricRegistry().meter(name("adapters", adapterConfig.getName(), "entriesConverted"));
        this.telemetryRegistry = telemetryRegistry;
        this.packages = Objects.requireNonNull(adapterConfig.getPackagesList());
    }

    @Override
    public void handleMessage(TelemetryMessage telemetryMessage) {
        LOG.debug("Received {} telemetry message", telemetryMessage);

        int flowPackets = 0;

        final List<Flow> flows = new LinkedList<>();
        try (Timer.Context ctx = logParsingTimer.time()) {
            this.entriesReceived.mark();

            LOG.trace("Parsing packet. ");
            final P flowPacket = parse(telemetryMessage);
            if (flowPacket != null) {
                this.entriesParsed.mark();

                flowPackets += 1;

                final List<Flow> converted = this.convert(flowPacket, Instant.ofEpochMilli(telemetryMessage.getTimestamp()));
                flows.addAll(converted);

                this.entriesConverted.mark(converted.size());
            }

            packetsPerLogHistogram.update(flowPackets);
        }

        final FlowSource source = new FlowSource(telemetryMessage.getFlowSource().getLocation(),
            telemetryMessage.getSourceAddress(),
            contextKey);

        LOG.debug("Sending Packets and flows to metrics-processor for Enrichment step. ");

        //
        telemetryRegistry.getDispatcher().send(TelemetryMessageProtoCreator.createMessage(source, flows,
            this.applicationThresholding, this.applicationDataCollection, this.packages));

        LOG.debug("Completed Adapter and Enrichment step for {} telemetry message.", telemetryMessage);

    }

    protected abstract P parse(TelemetryMessage message);

    protected abstract List<Flow> convert(final P packet, final Instant receivedAt);

    public String getMetaDataNodeLookup() {
        return metaDataNodeLookup;
    }

    public void setMetaDataNodeLookup(String metaDataNodeLookup) {
        this.metaDataNodeLookup = metaDataNodeLookup;

        if (!Strings.isNullOrEmpty(this.metaDataNodeLookup)) {
            this.contextKey = new ContextKey(metaDataNodeLookup);
        } else {
            this.contextKey = null;
        }
    }

    public boolean isApplicationThresholding() {
        return this.applicationThresholding;
    }

    public void setApplicationThresholding(final boolean applicationThresholding) {
        this.applicationThresholding = applicationThresholding;
    }

    public boolean isApplicationDataCollection() {
        return this.applicationDataCollection;
    }

    public void setApplicationDataCollection(final boolean applicationDataCollection) {
        this.applicationDataCollection = applicationDataCollection;
    }
}

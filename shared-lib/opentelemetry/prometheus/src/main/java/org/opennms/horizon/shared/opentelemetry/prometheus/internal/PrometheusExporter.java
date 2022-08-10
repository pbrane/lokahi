package org.opennms.horizon.shared.opentelemetry.prometheus.internal;

import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServerBuilder;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.export.CollectionRegistration;
import io.opentelemetry.sdk.metrics.export.MetricReader;

public class PrometheusExporter implements MetricReader {

    // copied over from prometheus exporter builder as these are kept private there
    private static final int DEFAULT_PORT = 9464;
    private static final String DEFAULT_HOST = "0.0.0.0";

    private final PrometheusHttpServerBuilder serverBuilder;
    private PrometheusHttpServer server;

    public PrometheusExporter() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public PrometheusExporter(String host, int port) {
        this.serverBuilder = PrometheusHttpServer.builder()
            .setHost(host).setPort(port);
    }

    public void start() {
        server = serverBuilder.build();
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    @Override
    public void register(CollectionRegistration collectionRegistration) {
        server.register(collectionRegistration);
    }

    @Override
    public CompletableResultCode forceFlush() {
        return server.forceFlush();
    }

    @Override
    public CompletableResultCode shutdown() {
        return server.shutdown();
    }

    @Override
    public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
        return server.getAggregationTemporality(instrumentType);
    }
}

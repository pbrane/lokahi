package org.opennms.horizon.shared.opentelemetry.core.internal;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.MetricReader;

public class SdkMeterProviderFactory {

    private final MetricReader metricReader;

    public SdkMeterProviderFactory(MetricReader metricReader) {
        this.metricReader = metricReader;
    }

    public MeterProvider create() {
        return SdkMeterProvider.builder()
            .registerMetricReader(metricReader)
            .build();
    }

}

package org.opennms.horizon.minion.opentelemetry.core.internal;

import io.opentelemetry.api.metrics.MeterBuilder;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.metrics.export.MetricReader;

public class MeterProviderWrapper implements MeterProvider {

    private final MeterProvider meterProvider;

    public MeterProviderWrapper(MetricReader metricReader) {
        this.meterProvider = new SdkMeterProviderFactory(metricReader)
            .create();
    }

    @Override
    public MeterBuilder meterBuilder(String instrumentationScopeName) {
        return meterProvider.meterBuilder(instrumentationScopeName);
    }

}

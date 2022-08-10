package org.opennms.horizon.minion.opentelemetry.core.internal;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;

public class OpenTelemetryWrapper implements OpenTelemetry {

    private final MeterProvider meterProvider;
    private final TracerProvider tracerProvider;
    private final ContextPropagators contextPropagators;

    public OpenTelemetryWrapper(MeterProvider meterProvider) {
        this(meterProvider, TracerProvider.noop(), ContextPropagators.noop());
    }

    public OpenTelemetryWrapper(MeterProvider meterProvider, TracerProvider tracerProvider, ContextPropagators contextPropagators) {
        this.meterProvider = meterProvider;
        this.tracerProvider = tracerProvider;
        this.contextPropagators = contextPropagators;
    }

    @Override
    public TracerProvider getTracerProvider() {
        return tracerProvider;
    }

    @Override
    public ContextPropagators getPropagators() {
        return contextPropagators;
    }

    @Override
    public MeterProvider getMeterProvider() {
        return meterProvider;
    }


}

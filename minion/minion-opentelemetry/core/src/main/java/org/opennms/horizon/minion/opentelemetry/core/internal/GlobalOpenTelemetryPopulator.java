package org.opennms.horizon.minion.opentelemetry.core.internal;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;

public class GlobalOpenTelemetryPopulator {

    private final OpenTelemetry openTelemetry;

    public GlobalOpenTelemetryPopulator(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    public void initialize() {
        GlobalOpenTelemetry.set(openTelemetry);
    }

    public void destroy() {
        // reset state of global telemetry
        GlobalOpenTelemetry.set(null);
    }

}

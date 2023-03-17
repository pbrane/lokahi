package org.opennms.horizon.tenantmetrics.impl;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.opennms.horizon.tenantmetrics.TenantMetricsTracker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TenantMetricsTrackerImpl implements TenantMetricsTracker {

    public static final String METRIC_SAMPLE_COUNT_NAME = "metric_sample_count";
    public static final String METRIC_SAMPLE_COUNT_TENANT_LABEL_NAME = "tenant";

    @Autowired
    private MeterRegistry meterRegistry;

    @Override
    public void addTenantMetricSampleCount(String tenant, int count) {
        Counter counter =
            meterRegistry.counter(METRIC_SAMPLE_COUNT_NAME,
                List.of(
                    Tag.of(METRIC_SAMPLE_COUNT_TENANT_LABEL_NAME, tenant)
                ));

        counter.increment(count);
    }
}

package org.opennms.horizon.tenantmetrics;

public interface TenantMetricsTracker {
    void addTenantMetricSampleCount(String tenant, int count);
}

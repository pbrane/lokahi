package org.opennms.horizon.tsdata;

public class MetricNameConstants {
    public static final String METRICS_NAME_PREFIX_MONITOR = "monitor_";
    public static final String METRICS_NAME_RESPONSE = "response_time_msec";
    public static final String METRIC_NAME_LABEL = "__name__";

    public static final String[] MONITOR_METRICS_LABEL_NAMES = {
        "instance",
        "location",
        "system_id",
        "monitor",
        "node_id"};
}

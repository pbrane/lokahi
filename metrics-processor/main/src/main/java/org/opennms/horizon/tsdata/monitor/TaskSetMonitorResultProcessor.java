package org.opennms.horizon.tsdata.monitor;

import org.opennms.horizon.timeseries.cortex.CortexTSS;
import org.opennms.horizon.tsdata.MetricNameConstants;
import org.opennms.taskset.contract.MonitorResponse;
import org.opennms.taskset.contract.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import prometheus.PrometheusTypes;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Component
public class TaskSetMonitorResultProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(TaskSetMonitorResultProcessor.class);

    @Autowired
    private CortexTSS cortexTSS;

    public void processMonitorResponse(String tenantId, TaskResult taskResult, MonitorResponse monitorResponse) throws IOException {
        LOG.info("Have monitor response, tenant-id: {}; task-id={};", tenantId, taskResult.getId());

        String[] labelValues =
            {
                monitorResponse.getIpAddress(),
                taskResult.getLocation(),
                taskResult.getSystemId(),
                monitorResponse.getMonitorType().name(),
                String.valueOf(monitorResponse.getNodeId())
            };

        PrometheusTypes.TimeSeries.Builder builder = PrometheusTypes.TimeSeries.newBuilder();

        addLabels(monitorResponse, labelValues, builder);

        builder.addLabels(PrometheusTypes.Label.newBuilder()
            .setName(MetricNameConstants.METRIC_NAME_LABEL)
            .setValue(CortexTSS.sanitizeMetricName(MetricNameConstants.METRICS_NAME_RESPONSE)));

        builder.addSamples(PrometheusTypes.Sample.newBuilder()
            .setTimestamp(Instant.now().toEpochMilli())
            .setValue(monitorResponse.getResponseTimeMs()));

        cortexTSS.store(tenantId, builder);

        for (Map.Entry<String, Double> entry : monitorResponse.getMetricsMap().entrySet()) {
            processMetricMaps(entry, monitorResponse, labelValues, tenantId);
        }
    }

//========================================
// Internals
//----------------------------------------
    private void processMetricMaps(Map.Entry<String, Double> entry, MonitorResponse response, String[] labelValues, String tenantId) throws IOException {
        prometheus.PrometheusTypes.TimeSeries.Builder builder = prometheus.PrometheusTypes.TimeSeries.newBuilder();
        String key = entry.getKey();
        Double value = entry.getValue();

        addLabels(response, labelValues, builder);

        builder.addLabels(PrometheusTypes.Label.newBuilder()
            .setName(MetricNameConstants.METRIC_NAME_LABEL)
            .setValue(CortexTSS.sanitizeMetricName(MetricNameConstants.METRICS_NAME_PREFIX_MONITOR + key)));

        builder.addSamples(PrometheusTypes.Sample.newBuilder()
            .setTimestamp(Instant.now().toEpochMilli())
            .setValue(value));

        cortexTSS.store(tenantId, builder);
    }

    private void addLabels(MonitorResponse response, String[] labelValues, PrometheusTypes.TimeSeries.Builder builder) {
        for (int i = 0; i < MetricNameConstants.MONITOR_METRICS_LABEL_NAMES.length; i++) {
            if (!"node_id".equals(MetricNameConstants.MONITOR_METRICS_LABEL_NAMES[i]) || !"ECHO".equals(response.getMonitorType().name())) {
                builder.addLabels(PrometheusTypes.Label.newBuilder()
                    .setName(CortexTSS.sanitizeLabelName(MetricNameConstants.MONITOR_METRICS_LABEL_NAMES[i]))
                    .setValue(CortexTSS.sanitizeLabelValue(labelValues[i])));
            }
        }
    }
}

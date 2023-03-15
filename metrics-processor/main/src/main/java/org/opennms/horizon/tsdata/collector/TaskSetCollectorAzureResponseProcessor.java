package org.opennms.horizon.tsdata.collector;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import org.opennms.horizon.azure.api.AzureResponseMetric;
import org.opennms.horizon.azure.api.AzureResultMetric;
import org.opennms.horizon.azure.api.AzureValueType;
import org.opennms.horizon.timeseries.cortex.CortexTSS;
import org.opennms.horizon.tsdata.MetricNameConstants;
import org.opennms.taskset.contract.CollectorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import prometheus.PrometheusTypes;

import java.time.Instant;

@Component
public class TaskSetCollectorAzureResponseProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(TaskSetCollectorAzureResponseProcessor.class);
    
    @Autowired
    private CortexTSS cortexTSS;

    public void processAzureCollectorResponse(String tenantId, CollectorResponse response, String[] labelValues) throws InvalidProtocolBufferException {
        Any collectorMetric = response.getResult();
        var azureResponse = collectorMetric.unpack(AzureResponseMetric.class);
        long now = Instant.now().toEpochMilli();

        for (AzureResultMetric azureResult : azureResponse.getResultsList()) {
            try {
                PrometheusTypes.TimeSeries.Builder builder = PrometheusTypes.TimeSeries.newBuilder();
                builder.addLabels(PrometheusTypes.Label.newBuilder()
                    .setName(MetricNameConstants.METRIC_NAME_LABEL)
                    .setValue(CortexTSS.sanitizeMetricName(azureResult.getAlias())));

                for (int i = 0; i < MetricNameConstants.MONITOR_METRICS_LABEL_NAMES.length; i++) {
                    builder.addLabels(PrometheusTypes.Label.newBuilder()
                        .setName(CortexTSS.sanitizeLabelName(MetricNameConstants.MONITOR_METRICS_LABEL_NAMES[i]))
                        .setValue(CortexTSS.sanitizeLabelValue(labelValues[i])));
                }

                int type = azureResult.getValue().getTypeValue();
                switch (type) {
                    case AzureValueType.INT64_VALUE:
                        builder.addSamples(PrometheusTypes.Sample.newBuilder()
                            .setTimestamp(now)
                            .setValue(azureResult.getValue().getUint64()));
                        break;
                    default:
                        LOG.warn("Unrecognized azure value type");
                }

                cortexTSS.store(tenantId, builder);
            } catch (Exception e) {
                LOG.warn("Exception parsing azure metrics ", e);
            }
        }
    }
}

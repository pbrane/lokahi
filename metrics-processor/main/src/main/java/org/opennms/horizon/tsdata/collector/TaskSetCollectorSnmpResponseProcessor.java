package org.opennms.horizon.tsdata.collector;

import com.google.protobuf.Any;
import org.opennms.horizon.snmp.api.SnmpResponseMetric;
import org.opennms.horizon.snmp.api.SnmpResultMetric;
import org.opennms.horizon.snmp.api.SnmpValueType;
import org.opennms.horizon.timeseries.cortex.CortexTSS;
import org.opennms.horizon.tsdata.MetricNameConstants;
import org.opennms.taskset.contract.CollectorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import prometheus.PrometheusTypes;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;

@Component
public class TaskSetCollectorSnmpResponseProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(TaskSetCollectorSnmpResponseProcessor.class);

    @Autowired
    private CortexTSS cortexTSS;

    public void processSnmpCollectorResponse(String tenantId, CollectorResponse response, String[] labelValues) throws IOException {
        Any collectorMetric = response.getResult();
        var snmpResponse = collectorMetric.unpack(SnmpResponseMetric.class);
        long now = Instant.now().toEpochMilli();
        for (SnmpResultMetric snmpResult : snmpResponse.getResultsList()) {
            try {
                PrometheusTypes.TimeSeries.Builder builder = prometheus.PrometheusTypes.TimeSeries.newBuilder();
                builder.addLabels(PrometheusTypes.Label.newBuilder()
                    .setName(MetricNameConstants.METRIC_NAME_LABEL)
                    .setValue(CortexTSS.sanitizeMetricName(snmpResult.getAlias())));
                for (int i = 0; i < MetricNameConstants.MONITOR_METRICS_LABEL_NAMES.length; i++) {
                    builder.addLabels(prometheus.PrometheusTypes.Label.newBuilder()
                        .setName(CortexTSS.sanitizeLabelName(MetricNameConstants.MONITOR_METRICS_LABEL_NAMES[i]))
                        .setValue(CortexTSS.sanitizeLabelValue(labelValues[i])));
                }
                int type = snmpResult.getValue().getTypeValue();
                switch (type) {
                    case SnmpValueType.INT32_VALUE:
                        builder.addSamples(PrometheusTypes.Sample.newBuilder()
                            .setTimestamp(now)
                            .setValue(snmpResult.getValue().getSint64()));
                        break;
                    case SnmpValueType.COUNTER32_VALUE:
                        // TODO: Can't set a counter through prometheus API, may be possible with remote write
                    case SnmpValueType.TIMETICKS_VALUE:
                    case SnmpValueType.GAUGE32_VALUE:
                        builder.addSamples(PrometheusTypes.Sample.newBuilder()
                            .setTimestamp(now)
                            .setValue(snmpResult.getValue().getUint64()));
                        break;
                    case SnmpValueType.COUNTER64_VALUE:
                        double metric = new BigInteger(snmpResult.getValue().getBytes().toByteArray()).doubleValue();
                        builder.addSamples(PrometheusTypes.Sample.newBuilder()
                            .setTimestamp(now)
                            .setValue(metric));
                        break;
                }

                cortexTSS.store(tenantId, builder);
            } catch (Exception e) {
                LOG.warn("Exception parsing metrics ", e);
            }
        }
    }
}

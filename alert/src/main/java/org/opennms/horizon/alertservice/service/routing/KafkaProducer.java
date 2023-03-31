package org.opennms.horizon.alertservice.service.routing;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.opennms.horizon.alerts.proto.Alert;
import org.opennms.horizon.alerts.proto.ManagedObjectInstance;
import org.opennms.horizon.alertservice.api.AlertLifecyleListener;
import org.opennms.horizon.alertservice.api.AlertService;
import org.opennms.horizon.alertservice.db.entity.MonitorPolicy;
import org.opennms.horizon.alertservice.service.MonitorPolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
@PropertySource("classpath:application.yaml")
public class KafkaProducer implements AlertLifecyleListener {
    public static final String DEFAULT_ALARMS_TOPIC = "new-alerts";

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    private final AlertService alertService;

    private final MonitorPolicyService monitorPolicyService;

    @Value("${kafka.topics.new-alerts:" + DEFAULT_ALARMS_TOPIC + "}")
    private String kafkaTopic;

    @Autowired
    public KafkaProducer(@Qualifier("kafkaAlertProducerTemplate") KafkaTemplate<String, byte[]> kafkaTemplate, AlertService alertService, MonitorPolicyService monitorPolicyService) {
        this.kafkaTemplate = kafkaTemplate;
        this.alertService = Objects.requireNonNull(alertService);
        this.monitorPolicyService = Objects.requireNonNull(monitorPolicyService);
    }

    @PostConstruct
    public void init() {
        alertService.addListener(this);
    }

    @PreDestroy
    public void destroy() {
        alertService.removeListener(this);
    }

    @Override
    public void handleNewOrUpdatedAlert(Alert alert) {
        List<MonitorPolicy> matchingPolicies = matchAlertToMonitoringPolicies(alert);

        if (!matchingPolicies.isEmpty()) {
            var producerRecord = new ProducerRecord<>(
                kafkaTopic,
                toKey(alert),
                Alert.newBuilder(alert)
                    .addAllMonitoringPolicyId(matchingPolicies.stream().map(MonitorPolicy::getId).toList())
                    .build()
                    .toByteArray()
            );

            kafkaTemplate.send(producerRecord);
        }
    }

    @Override
    public void handleDeletedAlert(Alert alert) {
        var producerRecord = new ProducerRecord<String, byte[]>(kafkaTopic, toKey(alert), null);
        kafkaTemplate.send(producerRecord);
    }

    private String toKey(Alert alert) {
        return alert.getTenantId() + "-" + alert.getLocation();
    }

    private List<MonitorPolicy> matchAlertToMonitoringPolicies(Alert alert) {
        ManagedObjectInstance instance = alert.getManagedObject().getInstance();
        if (instance.hasNodeVal()) {
            return monitorPolicyService.findByNode(instance.getNodeVal().getNodeId(), alert.getTenantId());
        }

        return Collections.emptyList();
    }
}

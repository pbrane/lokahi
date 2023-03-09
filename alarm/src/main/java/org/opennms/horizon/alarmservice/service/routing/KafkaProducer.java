package org.opennms.horizon.alarmservice.service.routing;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.opennms.horizon.alarms.proto.Alarm;
import org.opennms.horizon.alarmservice.api.AlarmLifecyleListener;
import org.opennms.horizon.alarmservice.api.AlarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Objects;

@Component
@PropertySource("classpath:application.yaml")
public class KafkaProducer implements AlarmLifecyleListener {
    public static final String DEFAULT_ALARMS_TOPIC = "new-alarms";

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    private final AlarmService alarmService;

    @Value("${kafka.topics.new-alarms:" + DEFAULT_ALARMS_TOPIC + "}")
    private String kafkaTopic;

    @Autowired
    public KafkaProducer(@Qualifier("kafkaAlarmProducerTemplate") KafkaTemplate<String, byte[]> kafkaTemplate, AlarmService alarmService) {
        this.kafkaTemplate = kafkaTemplate;
        this.alarmService = Objects.requireNonNull(alarmService);
    }

    @PostConstruct
    public void init() {
        alarmService.addListener(this);
    }

    @PreDestroy
    public void destroy() {
        alarmService.removeListener(this);
    }

    @Override
    public void handleNewOrUpdatedAlarm(Alarm alarm) {
        var producerRecord = new ProducerRecord<>(kafkaTopic, toKey(alarm), alarm.toByteArray());
        kafkaTemplate.send(producerRecord);
    }

    @Override
    public void handleDeletedAlarm(Alarm alarm) {
        var producerRecord = new ProducerRecord<String, byte[]>(kafkaTopic, toKey(alarm), null);
        kafkaTemplate.send(producerRecord);
    }

    private String toKey(Alarm alarm) {
        return alarm.getTenantId() + "-" + alarm.getLocation();
    }
}

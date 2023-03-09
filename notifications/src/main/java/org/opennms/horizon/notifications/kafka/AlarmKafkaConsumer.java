package org.opennms.horizon.notifications.kafka;

import com.google.common.base.Strings;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.Context;
import org.opennms.horizon.alarms.proto.Alarm;
import org.opennms.horizon.notifications.exceptions.NotificationException;
import org.opennms.horizon.notifications.service.NotificationService;
import org.opennms.horizon.shared.constants.GrpcConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Service
public class AlarmKafkaConsumer {
    private final Logger LOG = LoggerFactory.getLogger(AlarmKafkaConsumer.class);

    @Autowired
    private NotificationService notificationService;

    @KafkaListener(
        topics = "${horizon.kafka.alarms.topic}",
        concurrency = "${horizon.kafka.alarms.concurrency}"
    )
    public void consume(@Payload byte[] data) {
        try {
            Alarm alarm = Alarm.parseFrom(data);
            if (Strings.isNullOrEmpty(alarm.getTenantId())) {
                LOG.warn("TenantId is empty, dropping alarm {}", alarm);
                return;
            }
            Context.current().withValue(GrpcConstants.TENANT_ID_CONTEXT_KEY, alarm.getTenantId()).run(()-> {
                consumeAlarm(alarm);
            });
        } catch (InvalidProtocolBufferException e) {
            LOG.error("Error while parsing Alarm. Payload: {}", Arrays.toString(data), e);
        }
    }

    public void consumeAlarm(Alarm alarm){
        try {
            notificationService.postNotification(alarm);
        } catch (NotificationException e) {
            // TODO: We need better resiliency. If a notification fails, do we want to retry? do we want to try another method?
            LOG.error("Exception sending alarm to notification service: {}", alarm, e);
        }
    }
}

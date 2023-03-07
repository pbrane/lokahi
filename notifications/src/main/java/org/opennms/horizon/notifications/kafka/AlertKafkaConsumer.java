package org.opennms.horizon.notifications.kafka;

import java.util.Map;
import java.util.Optional;

import org.opennms.horizon.notifications.exceptions.NotificationException;
import org.opennms.horizon.notifications.service.NotificationService;
import org.opennms.horizon.shared.constants.GrpcConstants;
import org.opennms.horizon.shared.dto.event.AlertDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import io.grpc.Context;

@Service
public class AlertKafkaConsumer {
    private final Logger LOG = LoggerFactory.getLogger(AlertKafkaConsumer.class);

    @Autowired
    private NotificationService notificationService;

    @KafkaListener(
        topics = "${horizon.kafka.alerts.topic}",
        concurrency = "${horizon.kafka.alerts.concurrency}"
    )
    public void consume(@Payload AlertDTO alert, @Headers Map<String, Object> headers) {

        LOG.info("Received alert from kafka {}", alert);
        Optional<String> tenantOptional = getTenantId(headers);
        if (tenantOptional.isEmpty()) {
           LOG.warn("TenantId is empty, dropping alert {}", alert);
           return;
        }
        String tenantId = tenantOptional.get();
        LOG.info("Received tenantIda {}", tenantId);

        Context.current().withValue(GrpcConstants.TENANT_ID_CONTEXT_KEY, tenantId).run(()->
        {
            consumeAlert(alert);
        });
    }

    public void consumeAlert(AlertDTO alert){
        try {
            notificationService.postNotification(alert);
        } catch (NotificationException e) {
            LOG.error("Exception sending alert to PagerDuty.", e);
        }
    }

    private Optional<String> getTenantId(Map<String, Object> headers) {
        Object tenantId = headers.get(GrpcConstants.TENANT_ID_KEY);
        if (tenantId instanceof byte[]) {
            return Optional.of(new String((byte[]) tenantId));
        }
        return Optional.empty();
    }
}

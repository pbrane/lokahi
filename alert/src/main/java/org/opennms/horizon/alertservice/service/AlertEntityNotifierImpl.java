/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.horizon.alertservice.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.opennms.horizon.alertservice.api.AlertEntityNotifier;
import org.opennms.horizon.alertservice.db.entity.Alert;
import org.opennms.horizon.alertservice.db.entity.Memo;
import org.opennms.horizon.alertservice.db.tenant.TenantLookup;
import org.opennms.horizon.alertservice.model.AlertDTO;
import org.opennms.horizon.alertservice.model.AlertSeverity;
import org.opennms.horizon.shared.constants.GrpcConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import io.grpc.Context;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@PropertySource("classpath:application.yaml")
public class AlertEntityNotifierImpl implements AlertEntityNotifier {
    private static final Logger LOG = LoggerFactory.getLogger(AlertEntityNotifierImpl.class);

    public static final String DEFAULT_ALERTS_TOPIC = "new-alerts";

    @Autowired
    @Qualifier("kafkaAlertProducerTemplate")
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    @Value("${kafka.topics.new-alerts:" + DEFAULT_ALERTS_TOPIC + "}")
    private String kafkaTopic;

//    private Set<AlertEntityListener> listeners = Sets.newConcurrentHashSet();

    @Autowired
    TenantLookup tenantLookup;

    @Override
    public void didCreateAlert(Alert alert) {
        AlertDTO alertDTO =  AlertMapper.INSTANCE.alertToAlertDTO(alert);

        Optional<String> tenantId = tenantLookup.lookupTenantId(Context.current());

        if (tenantId.isPresent()) {
            var producerRecord = new ProducerRecord<String, byte[]>(kafkaTopic, alertDTO.toString().getBytes());
            producerRecord.headers().add(new RecordHeader(GrpcConstants.TENANT_ID_KEY, tenantId.get().getBytes(StandardCharsets.UTF_8)));
            kafkaTemplate.send(producerRecord);
        } else {
            LOG.warn("No tenant on alert:" + alertDTO.getAlertId());
        }
    }

    @Override
    public void didUpdateAlertWithReducedEvent(Alert alert) {
//        forEachListener(l -> l.onAlertUpdatedWithReducedEvent(alert));
    }

    @Override
    public void didAcknowledgeAlert(Alert alert, String previousAckUser, Date previousAckTime) {
//        forEachListener(l -> l.onAlertAcknowledged(alert, previousAckUser, previousAckTime));
    }

    @Override
    public void didUnacknowledgeAlert(Alert alert, String previousAckUser, Date previousAckTime) {
//        forEachListener(l -> l.onAlertUnacknowledged(alert, previousAckUser, previousAckTime));
    }

    @Override
    public void didUpdateAlertSeverity(Alert alert, AlertSeverity previousSeverity) {
//        forEachListener(l -> l.onAlertSeverityUpdated(alert, previousSeverity));
    }

    @Override
    public void didArchiveAlert(Alert alert, String previousReductionKey) {
//        forEachListener(l -> l.onAlertArchived(alert, previousReductionKey));
    }

    @Override
    public void didDeleteAlert(Alert alert) {
//        forEachListener(l -> l.onAlertDeleted(alert));
    }

    @Override
    public void didUpdateStickyMemo(Alert alert, String previousBody, String previousAuthor, Date previousUpdated) {
//        forEachListener(l -> l.onStickyMemoUpdated(alert, previousBody, previousAuthor, previousUpdated));
    }

    @Override
    public void didUpdateReductionKeyMemo(Alert alert, String previousBody, String previousAuthor, Date previousUpdated) {
//        forEachListener(l -> l.onReductionKeyMemoUpdated(alert, previousBody, previousAuthor, previousUpdated));
    }

    @Override
    public void didDeleteStickyMemo(Alert alert, Memo memo) {
//        forEachListener(l -> l.onStickyMemoDeleted(alert, memo));
    }

    @Override
    public void didUpdateLastAutomationTime(Alert alert, Date previousLastAutomationTime) {
//        forEachListener(l -> l.onLastAutomationTimeUpdated(alert, previousLastAutomationTime));
    }

    @Override
    public void didUpdateRelatedAlerts(Alert alert, Set<Alert> previousRelatedAlerts) {
//        forEachListener(l -> l.onRelatedAlertsUpdated(alert, previousRelatedAlerts));
    }

//    @Override
//    public void didChangeTicketStateForAlert(Alert alert, TroubleTicketState previousState) {
//        forEachListener(l -> l.onTicketStateChanged(alert, previousState));
//    }

//    private void forEachListener(Consumer<AlertEntityListener> callback) {
//        for (AlertEntityListener listener : listeners) {
//            try {
//                callback.accept(listener);
//            } catch (Exception e) {
//                log.error("Error occurred while invoking listener: {}. Skipping.", listener, e);
//            }
//        }
//    }

//    public void onListenerRegistered(final AlertEntityListener listener, final Map<String,String> properties) {
//        log.debug("onListenerRegistered: {} with properties: {}", listener, properties);
//        listeners.add(listener);
//    }
//
//    public void onListenerUnregistered(final AlertEntityListener listener, final Map<String,String> properties) {
//        log.debug("onListenerUnregistered: {} with properties: {}", listener, properties);
//        listeners.remove(listener);
//    }

}

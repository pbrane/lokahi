/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.horizon.notifications.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.opennms.horizon.alarms.proto.Alarm;
import org.opennms.horizon.alarms.proto.AlarmType;
import org.opennms.horizon.model.common.proto.Severity;
import org.opennms.horizon.notifications.api.dto.PagerDutyEventAction;
import org.opennms.horizon.notifications.api.dto.PagerDutyEventDTO;
import org.opennms.horizon.notifications.api.dto.PagerDutyPayloadDTO;
import org.opennms.horizon.notifications.api.dto.PagerDutySeverity;
import org.opennms.horizon.notifications.dto.PagerDutyConfigDTO;
import org.opennms.horizon.notifications.exceptions.NotificationAPIException;
import org.opennms.horizon.notifications.exceptions.NotificationBadDataException;
import org.opennms.horizon.notifications.exceptions.NotificationConfigUninitializedException;
import org.opennms.horizon.notifications.exceptions.NotificationException;
import org.opennms.horizon.notifications.exceptions.NotificationInternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.HashMap;

@Service
public class PagerDutyAPIImpl implements PagerDutyAPI {
    private static final Logger LOG = LoggerFactory.getLogger(PagerDutyAPI.class);

    @Autowired
    PagerDutyDao pagerDutyDao;

    @Autowired
    RestTemplate restTemplate;

    @Value("${horizon.pagerduty.client}")
    String client;

    @Value("${horizon.pagerduty.clientURL}")
    String clientURL;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void postNotification(Alarm alarm) throws NotificationException {
        try {
            PagerDutyEventDTO event = getEvent(alarm);

            String baseUrl = "https://events.pagerduty.com/v2/enqueue";
            URI uri = new URI(baseUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/vnd.pagerduty+json;version=2");
            headers.set("Content-Type", "application/json");

            String eventJson = objectMapper.writeValueAsString(event);
            HttpEntity<String> requestEntity = new HttpEntity<>(eventJson, headers);

            LOG.info("Posting alarm with id={} for tenant={} to PagerDuty", alarm.getDatabaseId(), alarm.getTenantId());
            restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);
        } catch (URISyntaxException e) {
            throw new NotificationInternalException("Bad PagerDuty url", e);
        } catch (JsonProcessingException e) {
            throw new NotificationBadDataException("JSON error processing alarmDTO", e);
        } catch (RestClientException e) {
            throw new NotificationAPIException("PagerDuty API exception", e);
        } catch (InvalidProtocolBufferException e) {
            throw new NotificationInternalException("Failed to encode/decode alarm: " + alarm, e);
        }
    }

    @Override
    public void saveConfig(PagerDutyConfigDTO config) {
        pagerDutyDao.saveConfig(config);
    }

    private String getPagerDutyIntegrationKey() throws NotificationConfigUninitializedException {
        PagerDutyConfigDTO config = pagerDutyDao.getConfig();
        return config.getIntegrationKey();
    }

    private PagerDutyEventDTO getEvent(Alarm alarm) throws NotificationConfigUninitializedException, JsonProcessingException, InvalidProtocolBufferException {
        Instant now = Instant.now();

        PagerDutyEventDTO event = new PagerDutyEventDTO();
        PagerDutyPayloadDTO payload = new PagerDutyPayloadDTO();

        payload.setSummary(alarm.getLogMessage().trim());
        payload.setTimestamp(now.toString());
        payload.setSeverity(PagerDutySeverity.fromAlarmSeverity(alarm.getSeverity()));

        // Source: unique location of affected system
        payload.setSource(JsonFormat.printer()
            .omittingInsignificantWhitespace()
            .sortingMapKeys()
            .print(alarm.getManagedObject()));
        // Component: component responsible for the event
        payload.setComponent(alarm.getManagedObject().getType().name());
        // Group: logical grouping
        payload.setGroup(alarm.getLocation());
        // Class: type of event
        payload.setClazz(alarm.getUei());

        event.setRoutingKey(getPagerDutyIntegrationKey());
        event.setDedupKey(alarm.getReductionKey());

        if (Severity.CLEARED.equals(alarm.getSeverity()) || AlarmType.CLEAR.equals(alarm.getType())) {
            event.setEventAction(PagerDutyEventAction.RESOLVE);
        } else if (alarm.getIsAcknowledged()) {
            event.setEventAction(PagerDutyEventAction.ACKNOWLEDGE);
        } else {
            event.setEventAction(PagerDutyEventAction.TRIGGER);
        }

        // TODO: We need to determine what the external facing URL is for the client
        event.setClient(client);
        event.setClientUrl(clientURL);

        payload.setCustomDetails(new HashMap<>());
        // Put the whole alarm in the payload
        payload.getCustomDetails().put("alarm", JsonFormat.printer().includingDefaultValueFields().print(alarm));

        event.setPayload(payload);
        return event;
    }
}

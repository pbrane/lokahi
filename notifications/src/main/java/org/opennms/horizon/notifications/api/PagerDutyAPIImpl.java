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

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opennms.horizon.notifications.api.dto.AlertSeverity;
import org.opennms.horizon.notifications.api.dto.AlertType;
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
import org.opennms.horizon.shared.dto.event.AlertDTO;
import org.opennms.horizon.shared.dto.event.EventDTO;
import org.opennms.horizon.shared.dto.event.EventParameterDTO;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    @Override
    public void postNotification(AlertDTO alert) throws NotificationException {
        try {
            String event = getEvent(alert);

            String baseUrl = "https://events.pagerduty.com/v2/enqueue";
            URI uri = new URI(baseUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/vnd.pagerduty+json;version=2");
            headers.set("Content-Type", "application/json");

            HttpEntity<String> requestEntity = new HttpEntity<>(event, headers);

            LOG.info("Posting alert {} to PagerDuty", alert.getId());
            restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);
        } catch (URISyntaxException e) {
            throw new NotificationInternalException("Bad PagerDuty url", e);
        } catch (JsonProcessingException e) {
            throw new NotificationBadDataException("JSON error processing AlertDTO", e);
        } catch (RestClientException e) {
            throw new NotificationAPIException("PagerDuty API exception", e);
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

    private String getEvent(AlertDTO alert) throws NotificationConfigUninitializedException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Instant now = Instant.now();

        PagerDutyEventDTO event = new PagerDutyEventDTO();
        PagerDutyPayloadDTO payload = new PagerDutyPayloadDTO();

        payload.setSummary(alert.getLogMessage().trim());
        payload.setTimestamp(now.toString());
        AlertSeverity alertSeverity = AlertSeverity.get(alert.getSeverity());
        payload.setSeverity(PagerDutySeverity.fromAlertSeverity(alertSeverity));

        if (alert.getNodeLabel() != null) {
            payload.setSource(alert.getNodeLabel());
        } else {
            payload.setSource("unknown");
        }
        String managedObjectType = alert.getManagedObjectType();
        String managedObjectInstance = alert.getManagedObjectInstance();
        if (managedObjectType != null && managedObjectType.length() > 0 && managedObjectInstance != null && managedObjectInstance.length() > 0) {
            // Use the MO type/instance if set
            payload.setComponent(String.format("%s - %s", alert.getManagedObjectType(), alert.getManagedObjectInstance()));
        }
        payload.setGroup("todo");
        payload.setClazz("class");

        event.setRoutingKey(getPagerDutyIntegrationKey());
        event.setDedupKey(alert.getReductionKey());

        if (AlertSeverity.CLEARED.equals(alertSeverity) || AlertType.RESOLUTION.equals(alert.getType())) {
            event.setEventAction(PagerDutyEventAction.RESOLVE);
        } else if (alert.getAckUser() != null && alert.getAckUser().length() > 0) {
            event.setEventAction(PagerDutyEventAction.ACKNOWLEDGE);
        } else {
            event.setEventAction(PagerDutyEventAction.TRIGGER);
        }

        //TODO: Add in alert id into url
        event.setClient(client);
        event.setClientUrl(clientURL);

        payload.setCustomDetails(new HashMap<>());

        // Add all of the event parameters as custom details
        EventDTO lastEvent = alert.getLastEvent();
        if (lastEvent != null) {
            Map<String, Object> customDetails = eparmsToMap(lastEvent.getParameters());
            payload.getCustomDetails().putAll(customDetails);
        }

        // If the event parameters contains a field called 'alert', then the alert itself overwrites that (by design).
        JsonNode alertJson = objectMapper.convertValue(alert, JsonNode.class);
        payload.getCustomDetails().put("alert", alertJson);

        event.setPayload(payload);

        return objectMapper.writeValueAsString(event);
    }

    protected static Map<String, Object> eparmsToMap(List<EventParameterDTO> eparms) {
        final Map<String, Object> map = new LinkedHashMap<>();
        if (eparms == null) {
            return map;
        }
        eparms.forEach(p -> map.put(p.getName(), p.getValue()));
        return map;
    }
}

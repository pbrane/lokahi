package org.opennms.horizon.datachoices.service;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.opennms.horizon.datachoices.exception.DataChoicesRuntimeException;
import org.opennms.horizon.datachoices.service.dto.CollectionResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class UsageStatsHandlerClient {
    private static final String ENDPOINT = "/hs-usage-report";

    @Autowired
    private RestTemplate restTemplate;

    @Value("${datachoices.stats-url}")
    private String statsUrl;

    public void sendStats(CollectionResults results) {
        System.out.println("UsageStatsHandlerClient.sendStats");
        System.out.println("results = " + ReflectionToStringBuilder.toString(results));

        HttpEntity<CollectionResults> request = new HttpEntity<>(results);
        try {
            restTemplate.postForEntity(statsUrl + ENDPOINT, request, Object.class);
        } catch (RestClientException e) {
            throw new DataChoicesRuntimeException("Failed to perform usage stats request", e);
        }
    }
}

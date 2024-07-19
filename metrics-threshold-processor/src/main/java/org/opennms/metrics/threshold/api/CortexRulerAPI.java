/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.metrics.threshold.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opennms.horizon.metrics.threshold.proto.MetricsThresholdAlertRule;
import org.opennms.metrics.threshold.api.dto.CortexRulerAlertDTO;
import org.opennms.metrics.threshold.exceptions.MetricsThresholdAPIRetryableException;
import org.opennms.metrics.threshold.exceptions.MetricsThresholdProcessorAPIException;
import org.opennms.metrics.threshold.exceptions.MetricsThresholdProcessorBadDataException;
import org.opennms.metrics.threshold.exceptions.MetricsThresholdProcessorException;
import org.opennms.metrics.threshold.exceptions.MetricsThresholdProcessorInternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Slf4j
@Service
public class CortexRulerAPI {

    private static final Logger LOG = LoggerFactory.getLogger(CortexRulerAPI.class);

    private final RestTemplate restTemplate;

    private final RetryTemplate retryTemplate;

    private final CortexRulerAlertFactory cortexRulerALertFactory;

    private final Environment env;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void createCortexAlertRule(MetricsThresholdAlertRule metricsThresholdAlertRule)
            throws MetricsThresholdProcessorException {
        try {
            CortexRulerAlertDTO alertRuleDto = cortexRulerALertFactory.createAlertRule(metricsThresholdAlertRule);

            // todo
            String baseUrl = env.getProperty("CORTEX_WRITE_URL") + "/api/v1/rules/"
                    + metricsThresholdAlertRule.getRuleNamespace();
            URI uri = new URI(baseUrl);

            String eventJson = objectMapper.writeValueAsString(alertRuleDto);
            HttpEntity<String> requestEntity =
                    new HttpEntity<>(eventJson, getHttpHeaders(metricsThresholdAlertRule.getTenantId()));

            LOG.info(
                    "Posting metrics alert rule  with alertName={} for tenant={} to Cortex Ruler",
                    metricsThresholdAlertRule.getMetricThresholdName(),
                    metricsThresholdAlertRule.getTenantId());
            postCortexAlertRule(uri, requestEntity);
        } catch (URISyntaxException e) {
            throw new MetricsThresholdProcessorInternalException("Bad Cortex Ruler url", e);
        } catch (JsonProcessingException e) {
            throw new MetricsThresholdProcessorBadDataException("JSON error processing alertRuleDto", e);
        }
    }

    public void deleteAlertRuleOrNameSpace(String alertGroupName, String tenantId)
            throws MetricsThresholdProcessorException {

        String deleteUrl = env.getProperty("CORTEX_WRITE_URL") + "/api/v1/rules/" + alertGroupName;

        HttpEntity<Void> requestEntity = new HttpEntity<>(getHttpHeaders(tenantId));
        ResponseEntity<Void> responseEntity =
                restTemplate.exchange(deleteUrl, HttpMethod.DELETE, requestEntity, Void.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            LOG.info("Resource deleted successfully.");
        } else {
            LOG.info("Failed to delete resource. Status code: " + responseEntity.getStatusCode());
        }
    }

    private void postCortexAlertRule(URI uri, HttpEntity<String> requestEntity)
            throws MetricsThresholdProcessorException {
        retryTemplate.execute(ctx -> {
            try {
                restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);
                return true;
            } catch (RestClientException e) {
                Throwable cause = e.getMostSpecificCause();

                if (cause instanceof ResourceAccessException) {

                    throw new MetricsThresholdAPIRetryableException("Cortex API Exception", e);
                } else if (cause instanceof RestClientResponseException) {

                    HttpStatusCode responseStatus = ((RestClientResponseException) e).getStatusCode();
                    if (responseStatus == HttpStatus.TOO_MANY_REQUESTS || responseStatus.is5xxServerError()) {
                        throw new MetricsThresholdAPIRetryableException("Cortex API exception", e);
                    }
                }

                // Not retryable
                throw new MetricsThresholdProcessorAPIException("Cortex API exception", e);
            }
        });
    }

    private HttpHeaders getHttpHeaders(final String tenantId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("X-Scope-OrgID", tenantId);
        return headers;
    }
}

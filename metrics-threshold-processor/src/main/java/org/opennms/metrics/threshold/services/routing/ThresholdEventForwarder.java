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
package org.opennms.metrics.threshold.services.routing;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.opennms.horizon.metrics.threshold.proto.ThresholdAlertData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ThresholdEventForwarder {

    private static final Logger LOG = LoggerFactory.getLogger(ThresholdEventForwarder.class);

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    private final String thresholdEventTopic;

    public ThresholdEventForwarder(
            KafkaTemplate<String, byte[]> kafkaTemplate,
            @Value("${kafka.topics.threshold-events}") String thresholdEventTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.thresholdEventTopic = thresholdEventTopic;
    }

    public void sendThresholdEvents(ThresholdAlertData thresholdAlertData) {
        LOG.info(
                "Sending {} threshold alert to metrics threshold event topic for tenant: {}",
                thresholdAlertData.getAlertsCount(),
                thresholdAlertData.getTenantId());
        var producerRecord = new ProducerRecord<String, byte[]>(thresholdEventTopic, thresholdAlertData.toByteArray());
        kafkaTemplate.send(producerRecord);
    }
}

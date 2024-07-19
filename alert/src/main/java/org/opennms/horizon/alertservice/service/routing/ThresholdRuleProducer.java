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
package org.opennms.horizon.alertservice.service.routing;

import lombok.extern.slf4j.Slf4j;
import org.opennms.horizon.alertservice.config.KafkaTopicProperties;
import org.opennms.horizon.metrics.threshold.proto.MetricsThresholdAlertRule;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ThresholdRuleProducer {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    private final String kafkaTopic;

    public ThresholdRuleProducer(
            KafkaTemplate<String, byte[]> kafkaTemplate, KafkaTopicProperties kafkaTopicProperties) {
        this.kafkaTopic = kafkaTopicProperties.getThresholdRules();
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendThresholdAlertRule(MetricsThresholdAlertRule metricsThresholdAlertRule) {
        log.info("Sending alert rules updates {}", metricsThresholdAlertRule);
        kafkaTemplate.send(kafkaTopic, metricsThresholdAlertRule.toByteArray());
    }
}

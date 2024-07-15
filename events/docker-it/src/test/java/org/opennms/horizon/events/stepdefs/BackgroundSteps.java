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
package org.opennms.horizon.events.stepdefs;

import io.cucumber.java.en.Given;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opennms.horizon.events.kafkahelper.KafkaTestHelper;

@RequiredArgsConstructor
@Slf4j
@Getter
public class BackgroundSteps {

    private final KafkaTestHelper kafkaTestHelper;

    private String kafkaBootstrapUrl;
    private String eventTopic;

    @Given("Kafka bootstrap URL in system property {string}")
    public void kafkaRestServerURLInSystemProperty(String systemProperty) {
        this.kafkaBootstrapUrl = System.getProperty(systemProperty);
        log.info("Using Kafka base URL: {}", this.kafkaBootstrapUrl);
        kafkaTestHelper.setKafkaBootstrapUrl(kafkaBootstrapUrl);
    }

    @Given("Kafka event topic {string}")
    public void createKafkaTopicForEvents(String eventTopic) {
        this.eventTopic = eventTopic;
        kafkaTestHelper.startConsumerAndProducer(eventTopic, eventTopic);
    }
}

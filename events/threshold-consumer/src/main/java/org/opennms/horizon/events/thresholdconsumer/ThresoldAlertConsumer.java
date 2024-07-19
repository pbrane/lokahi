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
package org.opennms.horizon.events.thresholdconsumer;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Arrays;
import lombok.Setter;
import org.opennms.horizon.events.proto.EventLog;
import org.opennms.horizon.events.traps.EventForwarder;
import org.opennms.horizon.metrics.threshold.proto.ThresholdAlertData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:application.yml")
public class ThresoldAlertConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(ThresoldAlertConsumer.class);

    @Autowired
    @Setter
    private ThresholdAlertToEventMapper thresholdAlertToEventMapper;

    @Autowired
    @Setter
    private EventForwarder eventForwarder;

    @KafkaListener(topics = "${kafka.threshold-events}", concurrency = "1")
    public void consumeMetricsThresholdEvents(@Payload byte[] data) {

        try {
            ThresholdAlertData thresholdAlertData = ThresholdAlertData.parseFrom(data);

            EventLog.Builder builder = EventLog.newBuilder().setTenantId(thresholdAlertData.getTenantId());

            thresholdAlertData.getAlertsList().forEach(element -> {
                builder.addEvents(thresholdAlertToEventMapper.convert(element));
            });

            EventLog eventLog = builder.build();
            eventForwarder.sendTrapEvents(eventLog);

        } catch (InvalidProtocolBufferException e) {
            LOG.error(
                    "Exception while parsing threshold alert  from payload. Events will be dropped. Payload: {}",
                    Arrays.toString(data),
                    e);
        }
    }
}

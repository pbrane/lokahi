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
package org.opennms.metrics.threshold.services;

import com.google.protobuf.util.JsonFormat;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.opennms.horizon.metrics.threshold.proto.ThresholdAlertData;
import org.opennms.metrics.threshold.services.routing.ThresholdEventForwarder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AlertThresholdProcessor {
    private Logger logger = LoggerFactory.getLogger(AlertThresholdProcessor.class);

    private final ThresholdEventForwarder thresholdEventForwarder;

    public void parsePayload(String payload) {
        ThresholdAlertData.Builder alertBuilder = ThresholdAlertData.newBuilder();
        try {
            JsonFormat.parser().ignoringUnknownFields().merge(payload, alertBuilder);
            String tanentId = alertBuilder.getAlertsList().stream()
                    .findFirst()
                    .map(x -> x.getLabels().get("tenant_id"))
                    .orElse("");
            alertBuilder.setTenantId(tanentId);
            ThresholdAlertData alert = alertBuilder.build();
            logger.info("Deserialized to Protobuf:");
            logger.info(" AlertThreshold Data  '{}' : ", alert);
            thresholdEventForwarder.sendThresholdEvents(alert);
        } catch (IOException e) {
            logger.warn(" Parsing failed  because of '{}' ", e.getMessage());
        }
    }
}

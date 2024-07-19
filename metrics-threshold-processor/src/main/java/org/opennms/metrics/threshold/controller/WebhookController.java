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
package org.opennms.metrics.threshold.controller;

import lombok.RequiredArgsConstructor;
import org.opennms.metrics.threshold.services.AlertThresholdProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class WebhookController {

    private Logger logger = LoggerFactory.getLogger(WebhookController.class);

    private final AlertThresholdProcessor alertThresholdProcessor;

    @PostMapping("/webhook/metrics-threshold")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload) {

        logger.info(" Webhook request received  ");
        // Process the payload received from Alertmanager
        // Add your logic here to handle the payload
        alertThresholdProcessor.parsePayload(payload);
        // Respond with a success message
        return ResponseEntity.status(HttpStatus.OK).body("Webhook received successfully");
    }
}

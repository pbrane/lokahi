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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opennms.horizon.metrics.threshold.proto.MetricsThresholdAlertRule;
import org.opennms.metrics.threshold.api.CortexRulerAPI;
import org.opennms.metrics.threshold.exceptions.MetricsThresholdProcessorException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class MetricsThresholdRulerService {

    private final CortexRulerAPI cortexRulerAPI;

    public void createMetricsAlertRule(MetricsThresholdAlertRule metricsThresholdAlertRule)
            throws MetricsThresholdProcessorException {

        if (metricsThresholdAlertRule.getAlertRulesList().isEmpty()) {
            log.info(
                    "Metrics threshold alert has no associated alert rules, dropping alert[monitory policy: {}, tenant: {}]",
                    metricsThresholdAlertRule.getRuleNamespace(),
                    metricsThresholdAlertRule.getTenantId());

            return;
        }

        try {
            cortexRulerAPI.createCortexAlertRule(metricsThresholdAlertRule);
        } catch (MetricsThresholdProcessorException exception) {
            log.warn(
                    "Unable to create alert rule [name: {}, tenant: {}] in Cortex Ruler:",
                    metricsThresholdAlertRule.getAlertGroup(),
                    metricsThresholdAlertRule.getTenantId(),
                    exception);
        }
    }

    public void deleteCortexAlertRule(final String alertRuleOrNameSpace, String tenantId) {

        try {
            cortexRulerAPI.deleteAlertRuleOrNameSpace(alertRuleOrNameSpace, tenantId);
        } catch (MetricsThresholdProcessorException exception) {
            log.warn(
                    "Unable to delete alert rule [name: {}, tenant: {}] in Cortex Ruler:",
                    alertRuleOrNameSpace,
                    tenantId,
                    exception);
        }
    }
}

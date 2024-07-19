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

import static org.opennms.horizon.shared.utils.SystemInfoUtils.DESCRIPTION;
import static org.opennms.horizon.shared.utils.SystemInfoUtils.SEVERITY;
import static org.opennms.horizon.shared.utils.SystemInfoUtils.SUMMERY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opennms.horizon.metrics.threshold.proto.MetricsThresholdAlertRule;
import org.opennms.metrics.threshold.api.dto.CortexRulerAlertDTO;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class CortexRulerAlertFactory {

    private final MetricsRulerExpressionGenerator metricsRulerExpressionGenerator;

    public CortexRulerAlertDTO createAlertRule(MetricsThresholdAlertRule metricsThresholdAlertRule) {

        List<CortexRulerAlertDTO.Rule> alertRules = new ArrayList<>();

        if (metricsThresholdAlertRule != null
                && !metricsThresholdAlertRule.getAlertRulesList().isEmpty()) {

            metricsThresholdAlertRule.getAlertRulesList().parallelStream().forEach(alertRule -> {

                // Populate labels for the rule
                Map<String, String> labels = new HashMap<>();
                labels.put("tenant_id", metricsThresholdAlertRule.getTenantId());
                labels.put(SEVERITY, alertRule.getSeverity());

                // Populate annotations for the rule
                Map<String, String> annotations = new HashMap<>();
                annotations.put(SUMMERY, alertRule.getSummary());
                annotations.put(DESCRIPTION, alertRule.getDescription());

                CortexRulerAlertDTO.Rule cortexRule = CortexRulerAlertDTO.Rule.builder()
                        .alert(alertRule.getAlertName())
                        .expr(metricsRulerExpressionGenerator.generateMetricsRulerExpression(
                                alertRule, metricsThresholdAlertRule.getMetricThresholdName()))
                        .forDuration(alertRule.getDuration())
                        .labels(labels)
                        .annotations(annotations)
                        .build();
                alertRules.add(cortexRule);
            });

            CortexRulerAlertDTO.Rule[] alertRulesArr = alertRules.toArray(new CortexRulerAlertDTO.Rule[0]);
            return CortexRulerAlertDTO.builder()
                    .name(metricsThresholdAlertRule.getAlertGroup())
                    .rules(alertRulesArr)
                    .build();
        }

        return null;
    }
}
